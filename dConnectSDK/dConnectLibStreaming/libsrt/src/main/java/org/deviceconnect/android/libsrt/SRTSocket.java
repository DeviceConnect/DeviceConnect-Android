package org.deviceconnect.android.libsrt;

/**
 * SRTソケット.
 */
public class SRTSocket {

    private long mSocketPtr = -1;

    private String mSocketAddress;

    private boolean mOpen = true;

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    void dumpStats() {
        NdkHelper.dumpStats(mSocketPtr);
    }

    public synchronized void send(final byte[] data, final int offset, final int length) throws SRTSocketException {
        if (!mOpen) {
            throw new SRTSocketException(0);
        }
        int result = NdkHelper.sendMessage(mSocketPtr, data, offset, length);
        if (result < 0) {
            throw new SRTSocketException(result);
        }
    }

    public String getSocketAddress() {
        return mSocketAddress;
    }

    public synchronized void close() {
        if (!mOpen) {
            return;
        }
        mOpen = false;
        NdkHelper.closeSrtSocket(mSocketPtr);
    }

    public boolean isClosed() {
        return !mOpen;
    }

    boolean isAvailable() {
        return mSocketPtr >= 0;
    }
}