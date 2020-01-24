package org.deviceconnect.android.libsrt;

import android.os.Handler;
import android.util.Log;

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
    }

    private static final String TAG = "SRT";

    private static final int DEFAULT_BACKLOG = 5;

    private final SRTServerSocket mServerSocket;

    private final List<SRTClientSocket> mClientSocketList = new ArrayList<>();

    private Thread mServerThread;

    private boolean mStarted;

    private final ServerEventListenerManager mServerEventListener = new ServerEventListenerManager();

    private final ClientEventListenerManager mClientEventListener = new ClientEventListenerManager();

    public SRTServer(final String serverAddress, final int serverPort, final int backlog) {
        mServerSocket = new SRTServerSocket(serverAddress, serverPort, backlog);
    }

    public SRTServer(final String serverAddress, final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    public String getServerAddress() {
        return mServerSocket.getServerAddress();
    }

    public int getServerPort() {
        return mServerSocket.getServerPort();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public synchronized void open() throws IOException {
        if (mStarted) {
            return;
        }

        NdkHelper.startup();
        try {
            mServerSocket.open();
            mStarted = true;
        } catch (IOException e) {
            mServerEventListener.onErrorOpen(this, (int) mServerSocket.mNativeSocket);
            throw e;
        }

        mServerThread = new Thread(() -> {
            try {
                while (mStarted && !Thread.interrupted()) {
                    if (DEBUG) {
                        Log.d(TAG, "Waiting for SRT client...");
                    }
                    SRTClientSocket socket = mServerSocket.accept();
                    if (DEBUG) {
                        Log.d(TAG, "NdkHelper.accept: client address = " + socket.getSocketAddress());
                    }

                    if (socket.isAvailable()) {
                        synchronized (mClientSocketList) {
                            mClientSocketList.add(socket);
                        }

                        mServerEventListener.onAcceptClient(this, socket);
                    } else {
                        close();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        mServerThread.setName("ServerThread");
        mServerThread.start();

        mServerEventListener.onOpen(this);
    }

    public void sendPacket(final byte[] packet) throws IOException {
        synchronized (mClientSocketList) {
            for (Iterator<SRTClientSocket> it = mClientSocketList.iterator(); it.hasNext(); ) {
                SRTClientSocket socket = it.next();
                try {
                    final int length = packet.length;
                    socket.send(packet, length);
                    mClientEventListener.onSendPacket(this, socket, length);
                } catch (ClientSocketException e) {
                    if (e.mError == -1) {
                        Log.e(TAG, "Client socket is closed.");
                        it.remove();
                    }
                    throw e;
                }
            }
        }
    }

    public synchronized void close() {
        if (!mStarted) {
            return;
        }

        mServerSocket.close();
        synchronized (mClientSocketList) {
            for (SRTClientSocket socket : mClientSocketList) {
                socket.close();
            }
            mClientSocketList.clear();
        }
        NdkHelper.cleanup();

        mStarted = false;
        mServerThread.interrupt();
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

    public static class ClientSocketException extends IOException {

        int mError;

        ClientSocketException(final int error) {
            super();
            mError = error;
        }
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
        public void onSendPacket(SRTServer server,
                                 SRTClientSocket clientSocket,
                                 int payloadByteSize) {
            listeners(l -> l.onSendPacket(server, clientSocket, payloadByteSize));
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
    }
}
