package org.deviceconnect.android.libsrt;

import java.io.IOException;

/**
 * SRTサーバーのソケット.
 *
 * このクラスはスレッドセーフではありません.
 */
public class SRTServerSocket {

    private static final int DEFAULT_BACKLOG = 5;

    private long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

    private boolean mIsOpen;

    public SRTServerSocket(final String serverAddress,
                           final int serverPort,
                           final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mBacklog = backlog;
    }

    public SRTServerSocket(final String serverAddress,
                           final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    public SRTServerSocket(final int serverPort) {
        this("0.0.0.0", serverPort);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public String getServerAddress() {
        return mServerAddress;
    }

    public int getServerPort() {
        return mServerPort;
    }

    public void open() throws IOException {
        if (!mIsOpen) {
            mNativeSocket = NdkHelper.createSrtSocket(mServerAddress, mServerPort, mBacklog);
            if (mNativeSocket < 0) {
                throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
            }

            mIsOpen = true;
        }
    }

    public SRTSocket accept() throws IOException {
        SRTSocket socket = new SRTSocket();
        NdkHelper.accept(mNativeSocket, socket);
        if (!mIsOpen) {
            return null;
        }
        if (!socket.isAvailable()) {
            throw new IOException("Failed to accept client.");
        }
        return socket;
    }

    public void close() {
        if (mIsOpen) {
            mIsOpen = false;
            NdkHelper.closeSrtSocket(mNativeSocket);
        }
    }
}
