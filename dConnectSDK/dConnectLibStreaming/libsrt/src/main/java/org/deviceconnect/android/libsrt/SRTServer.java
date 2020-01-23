package org.deviceconnect.android.libsrt;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SRTサーバー.
 */
public class SRTServer {

    /**
     * サーバーの動作状態に関するイベントを受信するリスナー.
     */
    public interface EventListener {

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
         * サーバーの開始に失敗した場合に実行されます.
         * @param server サーバーのインスタンス
         * @param error 失敗の原因
         */
        void onErrorOpen(SRTServer server, int error);
    }

    private static final String TAG = "SRT";

    private static final int DEFAULT_BACKLOG = 5;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

    private final List<ClientSocket> mClientSocketList = new ArrayList<>();

    private long mNativeSocket;

    private Thread mServerThread;

    private boolean mStarted;

    private final EventListenerManager mListenerManager = new EventListenerManager();

    public SRTServer(final String serverAddress, final int serverPort, final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mBacklog = backlog;
    }

    public SRTServer(final String serverAddress, final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    public String getServerAddress() {
        return mServerAddress;
    }

    public int getServerPort() {
        return mServerPort;
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
        mNativeSocket = NdkHelper.createSrtSocket(mServerAddress, mServerPort, mBacklog);
        if (mNativeSocket < 0) {
            mListenerManager.onErrorOpen(this, (int) mNativeSocket);
            throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
        }
        Log.d(TAG, "Created server socket: native pointer = " + mNativeSocket);
        mStarted = true;

        mServerThread = new Thread(() -> {
            try {
                while (mStarted && !Thread.interrupted()) {
                    Log.d(TAG, "Waiting for SRT client...");
                    long ptr = NdkHelper.accept(mNativeSocket, mServerAddress, mServerPort);
                    Log.d(TAG, "NdkHelper.accept: clientSocket = " + ptr);

                    if (ptr >= 0) {
                        synchronized (mClientSocketList) {
                            mClientSocketList.add(new ClientSocket(ptr));
                        }
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

        mListenerManager.onOpen(this);
    }

    public void sendPacket(final byte[] packet) throws IOException {
        synchronized (mClientSocketList) {
            for (Iterator<ClientSocket> it = mClientSocketList.iterator(); it.hasNext(); ) {
                ClientSocket socket = it.next();
                try {
                    socket.send(packet, packet.length);
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

        NdkHelper.closeSrtSocket(mNativeSocket);
        synchronized (mClientSocketList) {
            for (ClientSocket socket : mClientSocketList) {
                socket.close();
            }
            mClientSocketList.clear();
        }
        NdkHelper.cleanup();

        mStarted = false;
        mServerThread.interrupt();
        mServerThread = null;

        mListenerManager.onClose(this);
    }

    public void addEventListener(final EventListener listener, final Handler handler) {
        mListenerManager.addListener(listener, handler);
    }

    public void removeEventListener(final EventListener listener) {
        mListenerManager.removeListener(listener);
    }

    public static class ClientSocketException extends IOException {

        int mError;

        ClientSocketException(final int error) {
            super();
            mError = error;
        }
    }

    private static class ClientSocket {

        private final long mSocketPtr;

        private boolean mClosed;

        ClientSocket(final long ptr) {
            mSocketPtr = ptr;
        }

        public synchronized void send(final byte[] data, final int length) throws IOException {
            if (mClosed) {
                throw new IOException("already closed");
            }
            int result = NdkHelper.sendMessage(mSocketPtr, data, length);
            if (result < 0) {
                throw new ClientSocketException(result);
            }
        }

        synchronized void close() {
            mClosed = true;
            NdkHelper.closeSrtSocket(mSocketPtr);
        }
    }

    private static class EventListenerManager implements EventListener {

        final List<EventListenerWrapper> mEventListenerList = new ArrayList<>();

        void addListener(final EventListener listener, final Handler handler) {
            synchronized (mEventListenerList) {
                mEventListenerList.add(new EventListenerWrapper(listener, handler));
            }
        }

        void removeListener(final EventListener listener) {
            synchronized (mEventListenerList) {
                for (Iterator<EventListenerWrapper> it = mEventListenerList.iterator(); it.hasNext(); ) {
                    EventListenerWrapper cache = it.next();
                    if (listener == cache.mEventListener) {
                        cache.dispose();
                        it.remove();
                        return;
                    }
                }
            }
        }

        @Override
        public void onOpen(final SRTServer server) {
            synchronized (mEventListenerList) {
                for (EventListener l : mEventListenerList) {
                    l.onOpen(server);
                }
            }
        }

        @Override
        public void onClose(final SRTServer server) {
            synchronized (mEventListenerList) {
                for (EventListener l : mEventListenerList) {
                    l.onClose(server);
                }
            }
        }

        @Override
        public void onErrorOpen(final SRTServer server, final int error) {
            synchronized (mEventListenerList) {
                for (EventListener l : mEventListenerList) {
                    l.onErrorOpen(server, error);
                }
            }
        }
    }

    private static class EventListenerWrapper implements EventListener {

        EventListener mEventListener;

        Handler mHandler;

        EventListenerWrapper(final EventListener eventListener, final Handler handler) {
            mEventListener = eventListener;
            mHandler = handler;
        }

        void dispose() {
            mEventListener = null;
            mHandler = null;
        }

        @Override
        public void onOpen(final SRTServer server) {
            mHandler.post(() -> mEventListener.onOpen(server));
        }

        @Override
        public void onClose(final SRTServer server) {
            mHandler.post(() -> mEventListener.onClose(server));
        }

        @Override
        public void onErrorOpen(final SRTServer server, final int error) {
            mHandler.post(() -> mEventListener.onErrorOpen(server, error));
        }
    }
}
