package org.deviceconnect.android.libsrt;

import java.io.IOException;

/**
 * SRTサーバーのソケット.
 *
 * このクラスはスレッドセーフではありません.
 */
class SRTServerSocket {

    private long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

    private boolean mIsOpen;

    SRTServerSocket(final String serverAddress, final int serverPort, final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mBacklog = backlog;
    }

    public String getServerAddress() {
        return mServerAddress;
    }

    public int getServerPort() {
        return mServerPort;
    }

    void open() throws IOException {
        if (!mIsOpen) {
            mNativeSocket = NdkHelper.createSrtSocket(mServerAddress, mServerPort, mBacklog);
            if (mNativeSocket < 0) {
                throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
            }
            mIsOpen = true;
        }
    }

    SRTClientSocket accept() throws IOException {
        SRTClientSocket socket = new SRTClientSocket();
        NdkHelper.accept(mNativeSocket, socket);
        if (!socket.isAvailable()) {
            throw new IOException("Failed to accept client.");
        }
        return socket;
    }

    void close() {
        if (mIsOpen) {
            NdkHelper.closeSrtSocket(mNativeSocket);
            mIsOpen = false;
        }
    }
}
