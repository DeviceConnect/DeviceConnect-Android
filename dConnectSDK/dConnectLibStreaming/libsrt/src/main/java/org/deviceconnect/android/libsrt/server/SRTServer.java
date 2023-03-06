package org.deviceconnect.android.libsrt.server;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTServerSocket;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;

/**
 * SRTサーバー.
 */
public class SRTServer {
    /**
     * タグ.
     */
    private static final String TAG = "SRT-SERVER";

    /**
     * 接続できるクライアントの最大数を定義.
     */
    private static final int DEFAULT_MAX_CLIENT_NUM = 10;

    /**
     * 統計データを通知するインターバルのデフォルト値. 単位はミリ秒.
     */
    private static final long DEFAULT_STATS_INTERVAL = 5000;

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
     * 統計情報のリスナー.
     */
    private StatsListener mStatsListener;

    /**
     * 統計データをログ出力するタイマー.
     */
    private Timer mStatsTimer;

    /**
     * 統計データを通知するインターバル. 単位はミリ秒.
     */
    private long mStatsInterval = DEFAULT_STATS_INTERVAL;

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
    private final int mPort;

    /**
     * クライアントのソケットに設定するオプション.
     */
    private Map<Integer, Object> mCustomSocketOptions;

    /**
     * エラーが発生フラグ.
     *
     * SRTSession 内部でエラーが発生した場合に、このフラグは true になります。
     */
    private boolean mErrorFlag;

    /**
     * コンストラクタ.
     *
     * @param port サーバーのソケットにバインドするローカルのポート番号.
     */
    public SRTServer(final int port) {
        mPort = port;
    }

    /**
     * ソケットに反映するオプションを設定します.
     *
     * <p>
     * オプションはサーバ側のソケットがバインドされる前に設定されます.
     * </p>
     *
     * @param socketOptions ソケットに反映するオプションd
     */
    public void setSocketOptions(Map<Integer, Object> socketOptions) {
        mCustomSocketOptions = socketOptions;
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
            if (usesStats()) {
                startStatsTimer();
            } else {
                stopStatsTimer();
            }
        }
    }

    /**
     * SRT 統計データの LogCat へ表示するインターバルを設定します.
     *
     * <p>
     * {@link #setShowStats(boolean)} の前に実行すること.
     * </p>
     *
     * @param interval インターバル. 単位はミリ秒
     */
    public void setStatsInterval(long interval) {
        mStatsInterval = interval;
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
     * 統計情報のリスナーを設定します.
     *
     * @param statsListener リスナー
     */
    public void setStatsListener(final StatsListener statsListener) {
        mStatsListener = statsListener;
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
     * SRTServer への接続数を取得します.
     *
     * @return SRTServer への接続数
     */
    public int getConnectionCount() {
        synchronized (mSocketThreads) {
            return mSocketThreads.size();
        }
    }

    /**
     * SRTServer を開始します.
     *
     * @throws IOException SRTServer の開始に失敗した場合に発生
     */
    public synchronized void start() throws IOException {
        if (mServerStarted) {
            if (DEBUG) {
                Log.w(TAG, "SRTServer is already started.");
            }
            return;
        }

        if (mServerSocketThread != null) {
            mServerSocketThread.terminate();
            mServerSocketThread = null;
        }

        mServerSocket = new SRTServerSocket(mPort, mCustomSocketOptions);
        // This is obligatory only in live mode, if you predict to connect
        // to a peer with SRT version 1.2.0 or older. Not required since
        // 1.3.0, and all older versions support only live mode.
//        mServerSocket.setOption(SRT.SRTO_SENDER, true);
        // In order to make sure that the client supports non-live message
        // mode, let's require this.
       // mServerSocket.setOption(SRT.SRTO_MAXBW, 0L);

        mServerStarted = true;
        mErrorFlag = false;

        mServerSocketThread = new ServerSocketThread();
        mServerSocketThread.setName("SRT-SERVER-THREAD");
        mServerSocketThread.start();

        if (usesStats()) {
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
     * 統計情報を使用するかを確認します.
     *
     * @return 統計情報を使用する場合にはtrue、それ以外はfalse
     */
    private boolean usesStats() {
        return mStatsListener != null || mShowStats;
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

    private void notifyStats(final SRTSocket client, final SRTStats stats) {
        StatsListener listener = mStatsListener;
        if (listener != null) {
            listener.onStats(client, stats);
        }
    }

    /**
     * SRT 統計データを定期的に表示するためのタイマーを開始します.
     */
    private synchronized void startStatsTimer() {
        if (mStatsTimer == null) {
            mStatsTimer = new Timer("SRT-STATS");
            mStatsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mSocketThreads) {
                        for (SocketThread thread : mSocketThreads) {
                            SRTStats stats = thread.mClientSocket.getStats();
                            if (mShowStats) {
                                Log.d(TAG, "stats: " + stats);
                            }
                            notifyStats(thread.mClientSocket, stats);
                        }
                    }
                }
            }, mStatsInterval, mStatsInterval);
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
        mSRTSession = new SRTSession(mOnEventListener);
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
     * カスタムオプションを取得します.
     * <p>
     * オプションが存在しない婆には null を返却します.
     * </p>
     * @param option オプション
     * @return オプションの値
     */
    private Object getCustomOption(int option) {
        return mCustomSocketOptions == null ? null : mCustomSocketOptions.get(option);
    }

    /**
     * 全てのソケットを閉じます.
     */
    private void closeAllClientSocket() {
        synchronized (mSocketThreads) {
            for (SocketThread thread : mSocketThreads) {
                thread.terminate();
            }
        }
    }

    /**
     * クライアントソケットのスレッドを追加します.
     *
     * 最初にクライアントソケットに接続があった時に SRTSession を初期化して処理を開始します。
     *
     * @param socketThread 追加するソケットのスレッド
     */
    private void addClientSocketThread(SocketThread socketThread) {
        synchronized (mSocketThreads) {
            if (mSocketThreads.isEmpty()) {
                createSRTSession();
            }
            mSocketThreads.add(socketThread);
        }

        mSRTSession.addSRTClientSocket(socketThread.mClientSocket);
    }

    /**
     * クライアントソケットのスレッドを削除します.
     *
     * 全てのクライアントソケットが削除された時に、SRTSession を削除して処理を停止します。
     *
     * @param socketThread 削除するソケットのスレッド
     */
    private void removeClientSocketThread(SocketThread socketThread) {
        mSRTSession.removeSRTClientSocket(socketThread.mClientSocket);

        synchronized (mSocketThreads) {
            mSocketThreads.remove(socketThread);
            if (mSocketThreads.isEmpty()) {
                releaseSRTSession();
            }
        }
    }

    /**
     * SRTSession からのイベントを受信するリスナー.
     */
    private final SRTSession.OnEventListener mOnEventListener = new SRTSession.OnEventListener() {
        @Override
        public void onStarted() {
            if (DEBUG) {
                Log.d(TAG, "MediaStreamer started.");
            }
        }

        @Override
        public void onStopped() {
            if (DEBUG) {
                Log.d(TAG, "MediaStreamer stopped.");
            }
        }

        @Override
        public void onError(MediaEncoderException e) {
            if (DEBUG) {
                Log.e(TAG, "Error occurred on MediaStreamer.", e);
            }
            mErrorFlag = true;
            closeAllClientSocket();
        }
    };

    /**
     * SRT クライアントソケットの生存確認を行うスレッド.
     */
    private class SocketThread extends Thread {
        /**
         * 生存確認を行うソケット.
         */
        private final SRTSocket mClientSocket;

        SocketThread(final SRTSocket clientSocket) {
            mClientSocket = clientSocket;
            setName("SRT-CLIENT-" + clientSocket.getRemoteSocketAddress());
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
                // 事前にエラーがあった場合には、SRTSession を作成し直すために
                // 他の Socket が閉じて、SRTSession が削除されるのを待ちます。
                while (mErrorFlag) {
                    synchronized (mSocketThreads) {
                        if (mSocketThreads.isEmpty()) {
                            mErrorFlag = false;
                            break;
                        }
                    }
                    Thread.sleep(50);
                }

                addClientSocketThread(this);

                // ソケットにビットレートの最大値を設定
                Object inputbw = getCustomOption(SRT.SRTO_INPUTBW);
                if (!(inputbw instanceof Long)) {
                    mClientSocket.setOption(SRT.SRTO_INPUTBW, calcMaxBitRate());
                }

                Object oheadbw = getCustomOption(SRT.SRTO_OHEADBW);
                if (!(oheadbw instanceof Integer)) {
                    mClientSocket.setOption(SRT.SRTO_OHEADBW, 50);
                }

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
                if (DEBUG) {
                    Log.e(TAG, "Failed to start socket thread.", e);
                }
            } finally {
                try {
                    mClientSocket.close();
                } catch (Exception e) {
                    // ignore.
                }

                removeClientSocketThread(this);
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

    public interface StatsListener {
        /**
         * 指定したソケットの統計情報を通知します.
         *
         * @param client クライアント側のソケット
         * @param stats 統計情報
         */
        void onStats(SRTSocket client, SRTStats stats);
    }
}
