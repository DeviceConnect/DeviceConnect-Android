package org.deviceconnect.android.libsrt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;

/**
 * SRTクライアントのソケット.
 */
public class SRTClientSocket {

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

    public synchronized void send(final byte[] data, final int length) throws SRTClientSocketException {
        if (!mOpen) {
            throw new SRTClientSocketException(0);
        }
        int result = NdkHelper.sendMessage(mSocketPtr, data, length);
        if (result < 0) {
            throw new SRTClientSocketException(result);
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