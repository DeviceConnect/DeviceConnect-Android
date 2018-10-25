package org.deviceconnect.android.streaming.rtp;

import android.os.SystemClock;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RtpSocket {
    public static final String TAG = "RtpSocket";

    private AverageBitrate mAverageBitrate;
    private SenderThread mThread;

    private long mNativePtr;

    private long mCacheSize;
    private long mClock;
    private int mTTL = 1;

    /**
     * This RTP socket implements a buffering mechanism relying on a FIFO of buffers and a Thread.
     */
    public RtpSocket() {
        mCacheSize = 0;
        mAverageBitrate = new AverageBitrate();

        resetFifo();

        if (mThread == null) {
            mThread = new SenderThread();
            mThread.setPriority(Thread.MAX_PRIORITY);
            mThread.start();
        }
    }

    private void resetFifo() {
        mAverageBitrate.reset();
    }

    /**
     * Closes the underlying socket.
     */
    public void close() {
        if (mNativePtr == 0) {
            return;
        }

        if (mThread != null) {
            mThread.close();
            mThread = null;
        }

        RtpSocketNative.close(mNativePtr);
        mNativePtr = 0;
    }

    /**
     * Sets the SSRC of the stream.
     */
    public void setSSRC(int ssrc) {
        RtpSocketNative.setSSRC(mNativePtr, ssrc);
    }

    /**
     * Sets the clock frequency of the stream in Hz.
     */
    public void setClockFrequency(long clock) {
        mClock = clock;

        if (mNativePtr != 0) {
            RtpSocketNative.setClockFrequency(mNativePtr, clock);
        }
    }

    /**
     * Sets the Time To Live of the UDP packets.
     */
    public void setTimeToLive(int ttl) {
        mTTL = ttl;

        if (mNativePtr != 0) {
            RtpSocketNative.setTTL(mNativePtr, ttl);
        }
    }

    /**
     * Sets the destination address and to which the packets will be sent.
     */
    public void setDestination(InetAddress dest, int rtpPort, int rtcpPort) {
        if (rtpPort != 0 && rtcpPort != 0) {
            mNativePtr = RtpSocketNative.open(dest.getHostAddress(), rtpPort, rtcpPort);
            setSSRC(new Random().nextInt());
            setClockFrequency(mClock);
            setTimeToLive(mTTL);
        }
    }

    /**
     * Returns an approximation of the bitrate of the RTP stream in bits per second.
     */
    public long getBitrate() {
        return mAverageBitrate.average();
    }

    /**
     * 送信するデータを格納するインターフェース.
     */
    public interface Data {
        /**
         * H264 のデータ.
         * <p>
         * MEMO: データタイプは、NDK側でも定数を定義しているので注意すること。
         * </p>
         */
        int DATA_TYPE_H264 = 1;

        /**
         * MotionJpeg のデータ.
         */
        int DATA_TYPE_MJPEG = 2;

        /**
         * データのタイプを取得します.
         *
         * @return データタイプ
         */
        int getType();

        /**
         * データを取得します.
         *
         * @return データ
         */
        byte[] getBuffer();

        /**
         * データサイズを取得します.
         *
         * @return データサイズ
         */
        int getLength();

        /**
         * タイムスタンプを取得します.
         *
         * @return タイムスタンプ
         */
        long getTimestamp();

        /**
         * データの使用が終わったことを通知.
         */
        void release();
    }

    /**
     * データを送信します.
     *
     * @param data RTPで送信するデータ
     */
    public void send(Data data) {
        if (mThread != null && !mThread.isInterrupted()) {
            mThread.notifyData(data);
        }
    }

    /**
     * RTP送信用スレッド.
     */
    private class SenderThread extends Thread {

        /**
         * 送られてきたフレームバッファを一時的に格納するリスト.
         */
        private final List<Data> mPackets = new LinkedList<>();

        /**
         * スレッドをロックするためのオブジェクト.
         */
        private final Object mLockObject = new Object();

        /**
         * タイムスタンプ.
         */
        private long mOffsetTimestamp;

        /**
         * 通知するパケットを追加します.
         *
         * @param data パケット
         */
        void notifyData(Data data) {
            synchronized (mPackets) {
                mPackets.add(data);
            }

            synchronized (mLockObject) {
                mLockObject.notify();
            }
        }

        /**
         * スレッドを停止します.
         */
        void close() {
            interrupt();

            synchronized (mLockObject) {
                mLockObject.notify();
            }

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * The Thread sends the packets in the FIFO one by one at a constant rate.
         */
        @Override
        public void run() {
            Statistics stats = new Statistics(50, 3000);
            long oldTimestamp = 0;
            try {
                // Caches mCacheSize milliseconds of the stream in the FIFO.
                if (mCacheSize > 0) {
                    Thread.sleep(mCacheSize);
                }

                mOffsetTimestamp = 0;

                while (!isInterrupted()) {
                    // 通知するフレームバッファが存在しない場合にはスレッドを止めておく
                    if (mPackets.isEmpty()) {
                        try {
                            synchronized (mLockObject) {
                                mLockObject.wait();
                            }
                        } catch (InterruptedException e) {
                            // ignore.
                        }
                    }

                    if (!mPackets.isEmpty()) {
                        Data data;

                        synchronized (mPackets) {
                            data = mPackets.remove(0);
                        }

                        long currentTime = data.getTimestamp();
                        if (oldTimestamp != 0) {
                            final long delta = currentTime - oldTimestamp;
                            if (delta > 0) {
                                mOffsetTimestamp += delta;
                                stats.push(delta);
                            }
                        } else {
                            mOffsetTimestamp = System.nanoTime();
                        }

                        RtpSocketNative.send(mNativePtr, data.getType(), data.getBuffer(), data.getLength(),
                                System.nanoTime(), System.nanoTime());

                        oldTimestamp = currentTime;

                        mAverageBitrate.push(data.getLength());

                        data.release();
                    }
                }
            } catch (Exception e) {
                // ignore.
            }

            mThread = null;
            resetFifo();
        }
    }

    /**
     * Computes an average bit rate.
     **/
    protected static class AverageBitrate {

        private final static long RESOLUTION = 200;

        private long mOldNow, mNow, mDelta;
        private long[] mElapsed, mSum;
        private int mCount, mIndex, mTotal;
        private int mSize;

        AverageBitrate() {
            this(5000);
        }

        AverageBitrate(int delay) {
            mSize = delay / ((int) RESOLUTION);
            reset();
        }

        void reset() {
            mSum = new long[mSize];
            mElapsed = new long[mSize];
            mNow = SystemClock.elapsedRealtime();
            mOldNow = mNow;
            mCount = 0;
            mDelta = 0;
            mTotal = 0;
            mIndex = 0;
        }

        public void push(int length) {
            mNow = SystemClock.elapsedRealtime();
            if (mCount > 0) {
                mDelta += mNow - mOldNow;
                mTotal += length;
                if (mDelta > RESOLUTION) {
                    mSum[mIndex] = mTotal;
                    mTotal = 0;
                    mElapsed[mIndex] = mDelta;
                    mDelta = 0;
                    mIndex++;
                    if (mIndex >= mSize) mIndex = 0;
                }
            }
            mOldNow = mNow;
            mCount++;
        }

        int average() {
            long delta = 0;
            long sum = 0;

            for (int i = 0; i < mSize; i++) {
                sum += mSum[i];
                delta += mElapsed[i];
            }
            //Log.d(TAG, "Time elapsed: "+delta);
            return (int) (delta > 0 ? 8000 * sum / delta : 0);
        }

    }

    /**
     * Computes the proper rate at which packets are sent.
     */
    public static class Statistics {

        public final static String TAG = "Statistics";

        private final int count;

        private int c = 0;
        private float m = 0, q = 0;
        private long elapsed = 0;
        private long start = 0;
        private long duration = 0;
        private long period;
        private boolean initoffset = false;

        Statistics(int count, long period) {
            this.count = count;
            this.period = period * 1000000L;
        }

        public void push(long value) {
            duration += value;
            elapsed += value;
            if (elapsed > period) {
                elapsed = 0;
                long now = System.nanoTime();
                if (!initoffset || (now - start < 0)) {
                    start = now;
                    duration = 0;
                    initoffset = true;
                }
                value -= (now - start) - duration;
            }
            if (c < 40) {
                // We ignore the first 40 measured values because they may not be accurate
                c++;
                m = value;
            } else {
                m = (m * q + value) / (q + 1);
                if (q < count) q++;
            }
        }
    }
}
