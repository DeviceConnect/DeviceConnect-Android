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

    private static final int DEFAULT_BACKLOG = 5;

    private long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

    private final List<SRTClientSocket> mClientSocketList = new ArrayList<>();

    private boolean mIsOpen;

    private Thread mStatsThread;

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

    public SRTClientSocket accept() throws IOException {
        SRTClientSocket socket = new SRTClientSocket();
        NdkHelper.accept(mNativeSocket, socket);
        if (!mIsOpen) {
            return null;
        }
        if (!socket.isAvailable()) {
            throw new IOException("Failed to accept client.");
        }
        synchronized (mClientSocketList) {
            mClientSocketList.add(socket);
        }
        return socket;
    }

    public void close() {
        if (mIsOpen) {
            mIsOpen = false;

            stopDumpStats();

            NdkHelper.closeSrtSocket(mNativeSocket);
            synchronized (mClientSocketList) {
                for (SRTClientSocket socket : mClientSocketList) {
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
            for (SRTClientSocket socket : mClientSocketList) {
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
