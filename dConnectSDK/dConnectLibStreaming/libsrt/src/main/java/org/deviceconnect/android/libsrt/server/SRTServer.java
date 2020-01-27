package org.deviceconnect.android.libsrt.server;

import android.os.Handler;
import android.util.Log;

import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTServerSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;

/**
 * SRTサーバー.
 */
public class SRTServer {

    /**
     * サーバーの動作状態に関するイベントを受信するリスナー.
     */
    public interface ServerEventListener {

        /**
         * サーバーが開始したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         */
        void onStart(SRTServer server);

        /**
         * サーバーが停止したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         */
        void onStop(SRTServer server);

        /**
         * クライアントと接続したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         * @param clientSocket クライアント側のソケット情報
         */
        void onAcceptClient(SRTServer server, SRTSocket clientSocket);

        /**
         * サーバーの開始に失敗した場合に実行されます.
         *
         * @param server サーバーのインスタンス
         * @param error 失敗の原因
         */
        void onErrorStart(SRTServer server, int error);
    }

    /**
     * クライアントに関するイベントを受信するリスナー.
     */
    public interface ClientEventListener {

        /**
         * クライアントへデータを送信したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         * @param clientSocket クライアント側のソケット情報
         * @param payloadByteSize 送信したデータのサイズ. 単位はバイト
         */
        void onSendPacket(SRTServer server, SRTSocket clientSocket, int payloadByteSize);

        /**
         * クライアントへの送信に失敗したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         * @param clientSocket クライアント側のソケット情報
         */
        void onErrorSendPacket(SRTServer server, SRTSocket clientSocket);
    }

    private static final String TAG = "SRT";

    private static final long DEFAULT_STATS_INTERVAL = 5 * 1000;

    private final SRTServerSocket mServerSocket;

    private Thread mServerThread;

    /**
     * SRT サーバに接続されているクライアントを確認するスレッドのリスト.
     */
    private final List<SocketThread> mSocketThreads = new ArrayList<>();

    private boolean mIsStarted;

    private final ServerEventListenerManager mServerEventListener = new ServerEventListenerManager();

    private final ClientEventListenerManager mClientEventListener = new ClientEventListenerManager();

    /**
     * SRT の処理を行うセッション.
     */
    private SRTSession mSRTSession;

    /**
     * SRT サーバへのイベントを通知するコールバック.
     */
    private Callback mCallback;

    private boolean mStatsEnabled = false;

    private long mStatsInterval = DEFAULT_STATS_INTERVAL;

    public SRTServer(final String serverAddress, final int serverPort, final int maxClientNum) {
        mServerSocket = new SRTServerSocket(serverAddress, serverPort, maxClientNum);
    }

    public SRTServer(final String serverAddress, final int serverPort) {
        mServerSocket = new SRTServerSocket(serverAddress, serverPort);
    }

    public SRTServer(final int serverPort) {
        mServerSocket = new SRTServerSocket(serverPort);
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

    public void setStatsEnabled(boolean enable) {
        mStatsEnabled = enable;
    }

    public void setStatsInterval(final long statsInterval) {
        mStatsInterval = statsInterval;
    }

    public synchronized void start() throws IOException {
        if (mIsStarted) {
            return;
        }

        try {
            mServerSocket.open();
            if (mStatsEnabled) {
                mServerSocket.startDumpStats(mStatsInterval);
            }
            mIsStarted = true;
        } catch (IOException e) {
            mServerEventListener.onErrorStart(this, -1); // TODO エラーをJNIから取得
            throw e;
        }

        mServerThread = new Thread(() -> {
            try {
                while (mIsStarted && !Thread.interrupted()) {
                    if (DEBUG) {
                        Log.d(TAG, "Waiting for SRT client...");
                    }

                    SRTSocket socket = mServerSocket.accept();
                    if (socket != null) {
                        new SocketThread(socket).start();

                        if (DEBUG) {
                            Log.d(TAG, "NdkHelper.accept: client address = " + socket.getSocketAddress());
                        }

                        mServerEventListener.onAcceptClient(this, socket);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        mServerThread.setName("ServerThread");
        mServerThread.start();

        mServerEventListener.onStart(this);
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;

        mServerSocket.close();

        synchronized (mSocketThreads) {
            for (SocketThread t : mSocketThreads) {
                t.terminate();
            }
        }

        mServerThread.interrupt();
        try {
            mServerThread.join(100);
        } catch (InterruptedException e) {
            // ignore
        }
        mServerThread = null;

        mServerEventListener.onStop(this);
    }

    public void addServerEventListener(final ServerEventListener listener, final Handler handler) {
        mServerEventListener.addListener(new ServerEventListenerWrapper(listener, handler));
    }

    public void removeEventListener(final ServerEventListener listener) {
        mServerEventListener.removeListener(listener);
    }

    public void addClientEventListener(final ClientEventListener listener, final Handler handler) {
        mClientEventListener.addListener(new ClientEventListenerWrapper(listener, handler));
    }

    public void removeEventListener(final ClientEventListener listener) {
        mClientEventListener.removeListener(listener);
    }

    private static class ServerEventListenerManager
            extends EventListenerManager<ServerEventListener>
            implements ServerEventListener {

        @Override
        public void onStart(final SRTServer server) {
            listeners(l -> l.onStart(server));
        }

        @Override
        public void onStop(final SRTServer server) {
            listeners(l -> l.onStop(server));
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTSocket clientSocket) {
            listeners(l -> l.onAcceptClient(server, clientSocket));
        }

        @Override
        public void onErrorStart(final SRTServer server, final int error) {
            listeners(l -> l.onErrorStart(server, error));
        }
    }

    private static class ClientEventListenerManager
        extends EventListenerManager<ClientEventListener>
        implements ClientEventListener {

        @Override
        public void onSendPacket(final SRTServer server,
                                 final SRTSocket clientSocket,
                                 final int payloadByteSize) {
            listeners(l -> l.onSendPacket(server, clientSocket, payloadByteSize));
        }

        @Override
        public void onErrorSendPacket(final SRTServer server,
                                      final SRTSocket clientSocket) {
            listeners(l -> l.onErrorSendPacket(server, clientSocket));
        }
    }

    private static class ServerEventListenerWrapper
        extends EventListenerWrapper<ServerEventListener>
        implements ServerEventListener {

        ServerEventListenerWrapper(final ServerEventListener listener,
                                   final Handler handler) {
            super(listener, handler);
        }

        @Override
        public void onStart(final SRTServer server) {
            post(() -> mEventListener.onStart(server));
        }

        @Override
        public void onStop(final SRTServer server) {
            post(() -> mEventListener.onStop(server));
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTSocket clientSocket) {
            post(() -> mEventListener.onAcceptClient(server, clientSocket));
        }

        @Override
        public void onErrorStart(final SRTServer server, final int error) {
            post(() -> mEventListener.onErrorStart(server, error));
        }
    }

    private static class ClientEventListenerWrapper
        extends EventListenerWrapper<ClientEventListener>
        implements ClientEventListener {

        ClientEventListenerWrapper(final ClientEventListener listener,
                                   final Handler handler) {
            super(listener, handler);
        }

        @Override
        public void onSendPacket(final SRTServer server,
                                 final SRTSocket clientSocket,
                                 final int payloadByteSize) {
            post(() -> mEventListener.onSendPacket(server, clientSocket, payloadByteSize));
        }

        @Override
        public void onErrorSendPacket(final SRTServer server,
                                      final SRTSocket clientSocket) {
            post(() -> mEventListener.onErrorSendPacket(server, clientSocket));
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

        SocketThread(SRTSocket clientSocket) {
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
                    mSocketThreads.add(this);
                    if (mSocketThreads.size() == 1) {
                        createSRTSession();
                    }
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

                mServerSocket.removeSocket(mClientSocket);
            }
        }
    }

    public interface Callback {
        /**
         * SRTSession を作成時に呼び出します.
         *
         * <p>
         * この SRTSession にストリームを設定します。
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
