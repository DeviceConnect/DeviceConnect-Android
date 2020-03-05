package org.deviceconnect.android.libsrt.utils;

import org.deviceconnect.android.libsrt.SRTServerSocket;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTSocketException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TestSRTServer {
    private SRTServerSocket mServerSocket;
    private CountDownLatch mLatch;
    private AtomicBoolean mResult = new AtomicBoolean();
    private int mPort;

    private ServerSocketThread mThread;

    TestSRTServer(int port) {
        mPort = port;
    }

    public void start() throws Exception {
        if (mThread != null) {
            return;
        }

        mLatch = new CountDownLatch(1);

        mThread = new ServerSocketThread();
        mThread.start();

        if (!mLatch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("Server startup timed out.");
        }

        if (!mResult.get()) {
            throw new RuntimeException("Failed to launch a srt server.");
        }
    }

    public void stop() {
        if (mThread != null) {
            mThread.terminate();
            mThread = null;
        }
    }

    public abstract void execute(SRTSocket socket) throws SRTSocketException;

    private class ServerSocketThread extends Thread {
        void terminate() {
            interrupt();

            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (Exception e) {
                    // ignore.
                }
            }

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }

            mServerSocket = null;
        }

        @Override
        public void run() {
            try {
                mServerSocket = new SRTServerSocket(mPort);
                mServerSocket.open();
                mResult.set(true);
            } catch (Exception e) {
                mLatch.countDown();
                return;
            }

            mLatch.countDown();

            try {
                while (!isInterrupted()) {
                    try (SRTSocket socket = mServerSocket.accept()) {
                        execute(socket);
                        Thread.sleep(200);
                    }
                }
            } catch (Exception e) {
                // ignore.
            }
        }
    }
}
