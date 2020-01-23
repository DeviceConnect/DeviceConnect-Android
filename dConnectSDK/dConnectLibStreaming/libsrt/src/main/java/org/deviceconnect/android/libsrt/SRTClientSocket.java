package org.deviceconnect.android.libsrt;

import java.io.IOException;

public class SRTClientSocket {

    private long mSocketPtr = -1;

    private String mSocketAddress;

    private boolean mClosed;

    synchronized void send(final byte[] data, final int length) throws IOException {
        if (mClosed) {
            throw new IOException("already closed");
        }
        int result = NdkHelper.sendMessage(mSocketPtr, data, length);
        if (result < 0) {
            throw new SRTServer.ClientSocketException(result);
        }
    }

    public String getSocketAddress() {
        return mSocketAddress;
    }

    synchronized void close() {
        mClosed = true;
        NdkHelper.closeSrtSocket(mSocketPtr);
    }

    boolean isAvailable() {
        return mSocketPtr >= 0;
    }
}