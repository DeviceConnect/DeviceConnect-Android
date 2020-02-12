package org.deviceconnect.android.libsrt.server;

import android.util.Log;

import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTServerSocket;

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

    private static final int DEFAULT_MAX_CLIENT_NUM = 10;

    private final SRTServerSocket mServerSocket;

    private Thread mServerThread;

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
    private boolean mIsStarted;

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
     */
    private boolean mShowStats;

    /**
     * コンストラクタ.
     *
     * @param port サーバーのソケットにバインドするローカルのポート番号.
     */
    public SRTServer(final int port) {
        mServerSocket = new SRTServerSocket(port);
    }

    /**
     * SRT 統計データの LogCat への表示設定を行います.
     *
     * @param showStats LogCat に表示する場合はtrue、それ以外はfalse
     */
    public synchronized void setShowStats(boolean showStats) {
        mShowStats = showStats;

        // 既にサーバが開始されている場合は、タイマーの設定を行います。
        if (mIsStarted) {
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

    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    public String getServerAddress() {
        return mServerSocket.getServerAddress();
    }

    public int getServerPort() {
        return mServerSocket.getServerPort();
    }

    public SRTSession getSRTSession() {
        return mSRTSession;
    }

    public synchronized void start() throws IOException {
        if (mIsStarted) {
            return;
        }
        mServerSocket.open();
        mIsStarted = true;
        startServerThread();

        if (mShowStats) {
            startStatsTimer();
        }
    }

    private void startServerThread() {
        if (mServerThread != null) {
            if (DEBUG) {
                Log.d(TAG, "ServerThread is already running.");
            }
            return;
        }

        mServerThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    if (DEBUG) {
                        Log.d(TAG, "Waiting for SRT client...");
                    }

                    SRTSocket socket = mServerSocket.accept();

                    if (isMaxClientNum()) {
                        socket.close();
                    } else {
                        new SocketThread(socket).start();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        mServerThread.setName("SRTServerThread");
        mServerThread.start();
    }

    private boolean isMaxClientNum() {
        return mSocketThreads.size() >= mMaxClientNum;
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;

        stopStatsTimer();

        synchronized (mSocketThreads) {
            for (SocketThread t : mSocketThreads) {
                t.terminate();
            }
            mSocketThreads.clear();
        }

        mServerSocket.close();

        mServerThread.interrupt();
        try {
            mServerThread.join(100);
        } catch (InterruptedException e) {
            // ignore
        }
        mServerThread = null;
    }

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
                mClientSocket.setOptions(512 * 1024, 50);

                synchronized (mSocketThreads) {
                    if (mSocketThreads.isEmpty()) {
                        createSRTSession();
                    }
                    mSocketThreads.add(this);
                }

                mSRTSession.addSRTClientSocket(mClientSocket);

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
         * この SRTSession にエンコーダを設定します。
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
