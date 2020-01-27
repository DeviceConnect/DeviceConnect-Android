package org.deviceconnect.android.libsrt;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * SRTソケット.
 */
public class SRTSocket {

    private long mNativePtr = -1;

    private String mSocketAddress;

    private boolean mClosed;

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public String getSocketAddress() {
        return mSocketAddress;
    }

    void dumpStats() {
        NdkHelper.dumpStats(mNativePtr);
    }

    public boolean isClosed() {
        return mClosed;
    }

    public OutputStream getOutputStream() {
        // TODO 実装
        // Socket に合わせて、OutputStream を実装するべきか検討
        return null;
    }

    public InputStream getInputStream() {
        // TODO 実装
        // Socket に合わせて、InputStream を実装するべきか検討
        return null;
    }

    public synchronized void send(byte[] data) throws SRTSocketException {
        send(data, 0, data.length);
    }

    public synchronized void send(byte[] data, int dataLength) throws SRTSocketException {
        send(data, 0, dataLength);
    }

    public synchronized void send(final byte[] data, final int offset, final int length) throws SRTSocketException {
        if (mClosed) {
            throw new SRTSocketException(0);
        }
        int result = NdkHelper.sendMessage(mNativePtr, data, offset, length);
        if (result < 0) {
            throw new SRTSocketException(result);
        }
    }

    public synchronized int recv(byte[] data, int dataLength) throws SRTSocketException {
        if (mClosed) {
            throw new SRTSocketException("SRTSocket is already closed.", -1);
        }

        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }

        if (data.length < dataLength) {
            throw new IllegalArgumentException("dataLength is invalid.");
        }

        int result = NdkHelper.recvMessage(mNativePtr, data, dataLength);
        if (result < 0) {
            throw new SRTSocketException("Failed to received a message.", result);
        }
        return result;
    }

    public synchronized void close() {
        if (mClosed) {
            return;
        }
        mClosed = false;

        NdkHelper.closeSrtSocket(mNativePtr);
    }

    boolean isAvailable() {
        return mNativePtr >= 0;
    }
}