package org.deviceconnect.android.libsrt.server;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTServerSocket;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTSocketException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;

/**
 * SRTサーバー.
 */
public class SRTServer {

    private static final String TAG = "SRT";

    /**
     * 接続できるクライアントの最大数を定義.
     */
    private static final int DEFAULT_MAX_CLIENT_NUM = 10;

    /**
     * SRT サーバ.
     */
    private SRTServerSocket mServerSocket;

    /**
     * SRTServerSocket への接続を監視するスレッド.
     */
    private ServerSocketThread mServerSocketThread;

    /**
     * SRT サーバに接続されているクライアントを確認するスレッドのリスト.
     */
    private final List<SocketThread> mSocketThreads = new ArrayList<>();

    /**
     * 接続できる最大クライアント数.
     */
    private int mMaxClientNum = DEFAULT_MAX_CLIENT_NUM;

    /**
     * SRTServer の開始フラグ.
     * <p>
     * このフラグが true の場合は、SRTServer は動作中になります。
     * </p>
     */
    private boolean mServerStarted;

    /**
     * SRT の処理を行うセッション.
     */
    private SRTSession mSRTSession;

    /**
     * SRT サーバへのイベントを通知するコールバック.
     */
    private Callback mCallback;

    /**
     * 統計データをログ出力するタイマー.
     */
    private Timer mStatsTimer;

    /**
     * 統計データをログに出力フラグ.
     *
     * <p>
     * trueの場合は、ログを出力します。
     * </p>
     */
    private boolean mShowStats;

    /**
     * SRTサーバに設定するポート番号.
     */
    private int mPort;

    /**
     * コンストラクタ.
     *
     * @param port サーバーのソケットにバインドするローカルのポート番号.
     */
    public SRTServer(final int port) {
        mPort = port;
    }

    /**
     * SRT 統計データの LogCat への表示設定を行います.
     *
     * @param showStats LogCat に表示する場合はtrue、それ以外はfalse
     */
    public synchronized void setShowStats(boolean showStats) {
        mShowStats = showStats;

        // 既にサーバが開始されている場合は、タイマーの設定を行います。
        if (mServerStarted) {
            if (showStats) {
                startStatsTimer();
            } else {
                stopStatsTimer();
            }
        }
    }

    /**
     * 同時接続可能なクライアントの上限を設定します.
     *
     * {@link #start()} でサーバーを開始する前に設定してください.
     *
     * @param maxClientNum 同時接続可能なクライアントの最大個数
     */
    public void setMaxClientNum(final int maxClientNum) {
        mMaxClientNum = maxClientNum;
    }

    /**
     * SRTServer のイベントを通知するコールバックを設定します.
     *
     * クライアントから接続があった時に、{@link Callback#createSession(SRTSession)} が呼び出され
     * SRTSession を初期化します。
     *
     * SRTSession に設定された VideoEncoder、AudioEncoder によって、映像と音声を配信します。
     *
     * @param callback コールバック
     */
    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    /**
     * サーバの IP アドレスを取得します.
     *
     * @return IP アドレス
     */
    public String getServerAddress() {
        return mServerSocket.getServerAddress();
    }

    /**
     * サーバーのポート番号を取得します.
     *
     * @return ポート番号
     */
    public int getServerPort() {
        return mServerSocket.getServerPort();
    }

    /**
     * SRTSession を取得します.
     *
     * <p>
     * クライアントが接続されていない場合には SRTSession は開始されてないので null を返却します。
     * </p>
     *
     * @return SRTSession のインスタンス
     */
    public SRTSession getSRTSession() {
        return mSRTSession;
    }

    /**
     * SRTServer を開始します.
     *
     * @throws IOException SRTServer の開始に失敗した場合に発生
     */
    public synchronized void start() throws IOException {
        if (mServerStarted) {
            return;
        }

        mServerSocket = new SRTServerSocket(mPort);
        try {
            // TODO 他に設定する項目がないか検討
            mServerSocket.setOption(SRT.SRTO_SENDER, 1);
            mServerSocket.setOption(SRT.SRTO_MAXBW, 0L);
            mServerSocket.open();
        } catch (SRTSocketException e) {
            // SRT サーバの srt_bind と srt_listen に失敗した場合はサーバを閉じておく。
            mServerSocket.close();
            throw new IOException(e);
        }
        mServerStarted = true;

        mServerSocketThread = new ServerSocketThread();
        mServerSocketThread.setName("SRTServerThread");
        mServerSocketThread.start();

        if (mShowStats) {
            startStatsTimer();
        }
    }

    /**
     * SRTServer を停止します.
     *
     * 接続されていた SRTSocket も全て閉じます。
     */
    public synchronized void stop() {
        if (!mServerStarted) {
            return;
        }
        mServerStarted = false;

        stopStatsTimer();

        mServerSocketThread.terminate();
        mServerSocketThread = null;

        synchronized (mSocketThreads) {
            for (SocketThread t : mSocketThreads) {
                t.terminate();
            }
            mSocketThreads.clear();
        }
    }

    /**
     * 接続されたクライアント数が最大値を超えているか確認します.
     *
     * @return 最大値を超えている場合はtrue、それ以外はfalse
     */
    private boolean isMaxClientNum() {
        return mSocketThreads.size() >= mMaxClientNum;
    }

    /**
     * サーバソケットを監視するためのスレッド.
     */
    private class ServerSocketThread extends Thread {
        /**
         * スレッドの停止処理を行います.
         */
        void terminate() {
            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (Exception e) {
                    // ignore.
                }
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    if (DEBUG) {
                        Log.d(TAG, "Waiting for SRT client...");
                    }

                    SRTSocket socket = mServerSocket.accept();

                    if (isMaxClientNum()) {
                        // 接続の上限を超えている場合は新規ソケットは閉じます。
                        socket.close();
                    } else {
                        new SocketThread(socket).start();
                    }
                }
            } catch (Throwable e) {
                // ignore.
            } finally {
                if (mServerSocket != null) {
                    try {
                        mServerSocket.close();
                    } catch (Exception e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * SRT 統計データを定期的に表示するためのタイマーを開始します.
     */
    private synchronized void startStatsTimer() {
        if (mStatsTimer == null) {
            mStatsTimer = new Timer();
            mStatsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (SocketThread thread : mSocketThreads) {
                        thread.mClientSocket.dumpStats();
                    }
                }
            }, 0, 5 * 1000);
        }
    }

    /**
     * SRT 統計データを定期的に表示するためのタイマーを停止します.
     */
    private synchronized void stopStatsTimer() {
        if (mStatsTimer != null) {
            mStatsTimer.cancel();
            mStatsTimer = null;
        }
    }

    /**
     * SRT セッションを作成します.
     */
    private void createSRTSession() {
        if (mSRTSession != null) {
            releaseSRTSession();
        }
        mSRTSession = new SRTSession();
        if (mCallback != null) {
            mCallback.createSession(mSRTSession);
        }
        mSRTSession.start();
    }

    /**
     * SRT セッションを破棄します.
     */
    private void releaseSRTSession() {
        if (mSRTSession == null) {
            return;
        }
        mSRTSession.stop();
        if (mCallback != null) {
            mCallback.releaseSession(mSRTSession);
        }
        mSRTSession = null;
    }

    /**
     * VideoEncoder と AudioEncoder からビットレートの最大値を計算します.
     *
     * @return ビットレートの最大値
     */
    private long calcMaxBitRate() {
        long inputBW = 0;

        VideoEncoder videoEncoder = mSRTSession.getVideoEncoder();
        if (videoEncoder != null) {
            inputBW += videoEncoder.getVideoQuality().getBitRate();
        }

        AudioEncoder audioEncoder = mSRTSession.getAudioEncoder();
        if (audioEncoder != null) {
            inputBW += audioEncoder.getAudioQuality().getBitRate();
        }

        return inputBW;
    }

    /**
     * SRT クライアントソケットの生存確認を行うスレッド.
     */
    private class SocketThread extends Thread {
        /**
         * 生存確認を行うソケット.
         */
        private SRTSocket mClientSocket;

        SocketThread(final SRTSocket clientSocket) {
            mClientSocket = clientSocket;
        }

        void terminate() {
            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                synchronized (mSocketThreads) {
                    if (mSocketThreads.isEmpty()) {
                        createSRTSession();
                    }
                    mSocketThreads.add(this);
                }

                mSRTSession.addSRTClientSocket(mClientSocket);

                // TODO 他に設定するオプションがないか検討

                // ソケットの通信を非同期に設定
                mClientSocket.setOption(SRT.SRTO_RCVSYN, Boolean.FALSE);
                mClientSocket.setOption(SRT.SRTO_SNDSYN, Boolean.FALSE);

                // ソケットにビットレートの最大値を設定
                mClientSocket.setOption(SRT.SRTO_INPUTBW, calcMaxBitRate());
                mClientSocket.setOption(SRT.SRTO_OHEADBW, 50);

                while (!isInterrupted()) {
                    if (mClientSocket.isClosed()) {
                        // クライアントのソケットが閉じているので終了します
                        break;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                mSRTSession.removeSRTClientSocket(mClientSocket);

                try {
                    mClientSocket.close();
                } catch (Exception e) {
                    // ignore.
                }

                synchronized (mSocketThreads) {
                    mSocketThreads.remove(this);
                    if (mSocketThreads.isEmpty()) {
                        releaseSRTSession();
                    }
                }
            }
        }
    }

    public interface Callback {
        /**
         * SRTSession を作成時に呼び出します.
         *
         * <p>
         * 渡された SRTSession にエンコーダを設定します。
         * </p>
         *
         * @param session セッション
         */
        void createSession(SRTSession session);

        /**
         * SRTSession が破棄された時に呼び出します.
         *
         * @param session セッション
         */
        void releaseSession(SRTSession session);
    }
}
