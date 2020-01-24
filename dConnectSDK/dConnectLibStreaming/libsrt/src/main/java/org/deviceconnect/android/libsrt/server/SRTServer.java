package org.deviceconnect.android.libsrt.server;

import android.os.Handler;
import android.util.Log;

import org.deviceconnect.android.libsrt.SRTClientSocket;
import org.deviceconnect.android.libsrt.SRTClientSocketException;
import org.deviceconnect.android.libsrt.SRTServerSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
        void onOpen(SRTServer server);

        /**
         * サーバーが停止したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         */
        void onClose(SRTServer server);

        /**
         * クライアントと接続したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         * @param clientSocket クライアント側のソケット情報
         */
        void onAcceptClient(SRTServer server, SRTClientSocket clientSocket);

        /**
         * サーバーの開始に失敗した場合に実行されます.
         *
         * @param server サーバーのインスタンス
         * @param error 失敗の原因
         */
        void onErrorOpen(SRTServer server, int error);
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
        void onSendPacket(SRTServer server, SRTClientSocket clientSocket, int payloadByteSize);

        /**
         * クライアントへの送信に失敗したタイミングで実行されます.
         *
         * @param server サーバーのインスタンス
         * @param clientSocket クライアント側のソケット情報
         */
        void onErrorSendPacket(SRTServer server, SRTClientSocket clientSocket);
    }

    private static final String TAG = "SRT";

    private final SRTServerSocket mServerSocket;

    private Thread mServerThread;

    /**
     * SRT サーバに接続されているクライアントを確認するスレッドのリスト.
     */
    private final List<SocketThread> mSocketThreads = new ArrayList<>();

    private boolean mOpen;

    private final ServerEventListenerManager mServerEventListener = new ServerEventListenerManager();

    private final ClientEventListenerManager mClientEventListener = new ClientEventListenerManager();

    private final List<SRTClientSocket> mClientSocketList = new ArrayList<>();

    /**
     * SRT の処理を行うセッション.
     */
    private SRTSession mSRTSession;

    /**
     * SRT サーバへのイベントを通知するコールバック.
     */
    private Callback mCallback;

    public SRTServer(final String serverAddress, final int serverPort, final int backlog) {
        mServerSocket = new SRTServerSocket(serverAddress, serverPort, backlog);
    }

    public SRTServer(final String serverAddress, final int serverPort) {
        mServerSocket = new SRTServerSocket(serverAddress, serverPort);
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

    public synchronized void open() throws IOException {
        if (mOpen) {
            return;
        }

        try {
            mServerSocket.open();
            mOpen = true;
        } catch (IOException e) {
            mServerEventListener.onErrorOpen(this, -1); // TODO エラーをJNIから取得
            throw e;
        }

        mServerThread = new Thread(() -> {
            try {
                while (mOpen && !Thread.interrupted()) {
                    if (DEBUG) {
                        Log.d(TAG, "Waiting for SRT client...");
                    }

                    SRTClientSocket socket = mServerSocket.accept();
                    new SocketThread(socket).start();

                    if (DEBUG) {
                        Log.d(TAG, "NdkHelper.accept: client address = " + socket.getSocketAddress());
                    }
                    synchronized (mClientSocketList) {
                        mClientSocketList.add(socket);
                    }
                    mServerEventListener.onAcceptClient(this, socket);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                close();
            }
        });
        mServerThread.setName("ServerThread");
        mServerThread.start();

        mServerEventListener.onOpen(this);
    }

    public void sendPacket(final byte[] packet) {
        sendPacket(packet, packet.length);
    }

    public void sendPacket(final byte[] packet, final int length) {
        synchronized (mClientSocketList) {
            for (Iterator<SRTClientSocket> it = mClientSocketList.iterator(); it.hasNext(); ) {
                SRTClientSocket socket = it.next();
                try {
                    socket.send(packet, length);

                    if (DEBUG) {
                        mClientEventListener.onSendPacket(this, socket, length);
                    }
                } catch (SRTClientSocketException e) {
                    if (e.getError() == -1) {
                        Log.e(TAG, "Client socket is closed.");
                        it.remove();
                    }
                    mClientEventListener.onErrorSendPacket(this, socket);
                }
            }
        }
    }

    public synchronized void close() {
        if (!mOpen) {
            return;
        }
        mOpen = false;

        mServerSocket.close();
        synchronized (mClientSocketList) {
            for (SRTClientSocket socket : mClientSocketList) {
                socket.close();
            }
            mClientSocketList.clear();
        }

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

        mServerEventListener.onClose(this);
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
        public void onOpen(final SRTServer server) {
            listeners(l -> l.onOpen(server));
        }

        @Override
        public void onClose(final SRTServer server) {
            listeners(l -> l.onClose(server));
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTClientSocket clientSocket) {
            listeners(l -> l.onAcceptClient(server, clientSocket));
        }

        @Override
        public void onErrorOpen(final SRTServer server, final int error) {
            listeners(l -> l.onErrorOpen(server, error));
        }
    }

    private static class ClientEventListenerManager
        extends EventListenerManager<ClientEventListener>
        implements ClientEventListener {

        @Override
        public void onSendPacket(final SRTServer server,
                                 final SRTClientSocket clientSocket,
                                 final int payloadByteSize) {
            listeners(l -> l.onSendPacket(server, clientSocket, payloadByteSize));
        }

        @Override
        public void onErrorSendPacket(final SRTServer server,
                                      final SRTClientSocket clientSocket) {
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
        public void onOpen(final SRTServer server) {
            post(() -> mEventListener.onOpen(server));
        }

        @Override
        public void onClose(final SRTServer server) {
            post(() -> mEventListener.onClose(server));
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTClientSocket clientSocket) {
            post(() -> mEventListener.onAcceptClient(server, clientSocket));
        }

        @Override
        public void onErrorOpen(final SRTServer server, final int error) {
            post(() -> mEventListener.onErrorOpen(server, error));
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
                                 final SRTClientSocket clientSocket,
                                 final int payloadByteSize) {
            post(() -> mEventListener.onSendPacket(server, clientSocket, payloadByteSize));
        }

        @Override
        public void onErrorSendPacket(final SRTServer server,
                                      final SRTClientSocket clientSocket) {
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
        mSRTSession.configure();
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
        private SRTClientSocket mClientSocket;

        SocketThread(SRTClientSocket clientSocket) {
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

                mSRTSession.getVideoStream().addSRTClientSocket(mClientSocket);

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
                mSRTSession.getVideoStream().removeSRTClientSocket(mClientSocket);

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
