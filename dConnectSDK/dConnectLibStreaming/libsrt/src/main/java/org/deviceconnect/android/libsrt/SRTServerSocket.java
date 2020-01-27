package org.deviceconnect.android.libsrt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SRTサーバーのソケット.
 *
 * このクラスはスレッドセーフではありません.
 */
public class SRTServerSocket {

    private static final int DEFAULT_MAX_CLIENT_NUM = 10;

    private static final int DEFAULT_BACKLOG = 5;

    private long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mMaxClientNum;

    private final int mBacklog;

    private final List<SRTSocket> mClientSocketList = new ArrayList<>();

    private boolean mIsOpen;

    private Thread mStatsThread;

    public SRTServerSocket(final String serverAddress,
                           final int serverPort,
                           final int maxClientNum,
                           final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mMaxClientNum = maxClientNum;
        mBacklog = backlog;
    }

    public SRTServerSocket(final String serverAddress,
                           final int serverPort,
                           final int maxClientNum) {
        this(serverAddress, serverPort, maxClientNum, DEFAULT_BACKLOG);
    }

    public SRTServerSocket(final String serverAddress,
                           final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_MAX_CLIENT_NUM);
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
        if (isMaxClientNum()) {
            socket.close();
            return null;
        }
        synchronized (mClientSocketList) {
            mClientSocketList.add(socket);
        }
        return socket;
    }

    public void removeSocket(final SRTSocket socket) {
        synchronized (mClientSocketList) {
            mClientSocketList.remove(socket);
        }
    }

    private boolean isMaxClientNum() {
        return mClientSocketList.size() >= mMaxClientNum;
    }

    public void close() {
        if (mIsOpen) {
            mIsOpen = false;

            stopDumpStats();

            NdkHelper.closeSrtSocket(mNativeSocket);
            synchronized (mClientSocketList) {
                for (SRTSocket socket : mClientSocketList) {
                    socket.close();
                }
                mClientSocketList.clear();
            }
        }
    }

    public void startDumpStats(final long interval) {
        mStatsThread = new StatsThread(interval);
        mStatsThread.start();
    }

    public void stopDumpStats() {
        if (mStatsThread != null) {
            mStatsThread.interrupt();
            mStatsThread = null;
        }
    }

    private void dumpStats() {
        synchronized (mClientSocketList) {
            for (SRTSocket socket : mClientSocketList) {
                socket.dumpStats();
            }
        }
    }

    private class StatsThread extends Thread {

        /**
         * 統計データをデバッグログとして出力するインターバル. 単位はミリ秒.
         */
        private final long mStatsInterval;

        StatsThread(final long interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("interval is negative");
            }
            mStatsInterval = interval;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    dumpStats();

                    Thread.sleep(mStatsInterval);
                }
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }
}
