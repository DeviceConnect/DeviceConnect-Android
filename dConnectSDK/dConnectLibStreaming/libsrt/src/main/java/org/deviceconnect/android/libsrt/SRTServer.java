package org.deviceconnect.android.libsrt;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

/**
 * SRTサーバー.
 */
public class SRTServer {

    private static final String TAG = "SRT";

    private final String mAddress;

    private final int mPort;

    private final List<ClientSocket> mClientSocketList = new ArrayList<>();

    private long mNativeSocket;

    private Thread mServerThread;

    private QueueThread<Message> mMessageThread;

    private boolean mStarted;


    public SRTServer(final String address, final int port) {
        mAddress = address;
        mPort = port;
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    public synchronized void start() throws IOException {
        if (mStarted) {
            return;
        }

        NdkHelper.startup();
        mNativeSocket = NdkHelper.createSrtSocket(mAddress, mPort);
        if (mNativeSocket < 0) {
            throw new IOException("Failed to create server socket: " + mAddress + ":" + mPort);
        }
        Log.d(TAG, "Created server socket: native pointer = " + mNativeSocket);
        mStarted = true;

        mServerThread = new Thread(() -> {
            try {
                while (mStarted && !Thread.interrupted()) {
                    Log.d(TAG, "Waiting for SRT client...");
                    long ptr = NdkHelper.accept(mNativeSocket, mAddress, mPort);
                    Log.d(TAG, "NdkHelper.accept: clientSocket = " + ptr);

                    if (ptr >= 0) {
                        synchronized (mClientSocketList) {
                            mClientSocketList.add(new ClientSocket(ptr));
                        }
                    } else {
                        stop();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        mServerThread.setName("ServerThread");
        mServerThread.start();

        mMessageThread = new QueueThread<Message>() {
            @Override
            public void run() {
                try {
                    while (mStarted && !Thread.interrupted()) {
                        Message message = get();
                        if (message != null) {
                            try {
                                sendPacket(message.mData);
                            } catch (IOException ignored) {}
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        };
        mMessageThread.setName("MessageThread");
        mMessageThread.start();
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

    public synchronized void stop() {
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
        mMessageThread.interrupt();
        mMessageThread = null;
    }

    public void offer(final byte[] data, final int length) {
        offer(data, 0, length);
    }

    public void offer(final byte[] data, final int offset, final int length) {
        if (!mStarted) {
            throw new IllegalStateException();
        }
        mMessageThread.add(new Message(data, offset, length));
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

        public synchronized void close() {
            mClosed = true;
            NdkHelper.closeSrtSocket(mSocketPtr);
        }
    }

    private static class Message {
        final byte[] mData;
        final int mOffset;
        final int mLength;

        Message(final byte[] data, final int offset, final int length) {
            if (data == null) {
                throw new IllegalArgumentException("data is null");
            }
            if (offset < 0) {
                throw new IllegalArgumentException("offset is negative");
            }
            if (length < 0) {
                throw new IllegalArgumentException("length is negative");
            }
            mData = data;
            mOffset = offset;
            mLength = length;
        }
    }
}
