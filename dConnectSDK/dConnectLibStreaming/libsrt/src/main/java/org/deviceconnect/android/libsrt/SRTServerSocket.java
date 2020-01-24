package org.deviceconnect.android.libsrt;

import java.io.IOException;

/**
 * SRTサーバーのソケット.
 */
class SRTServerSocket {

    long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

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
        mNativeSocket = NdkHelper.createSrtSocket(mServerAddress, mServerPort, mBacklog);
        if (mNativeSocket < 0) {
            throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
        }
    }

    SRTClientSocket accept() {
        SRTClientSocket socket = new SRTClientSocket();
        NdkHelper.accept(mNativeSocket, socket);
        return socket;
    }

    void close() {
        NdkHelper.closeSrtSocket(mNativeSocket);
    }
}
