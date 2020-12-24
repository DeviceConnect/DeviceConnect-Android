package org.deviceconnect.android.deviceplugin.host.connection;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.M)
public class HostTrafficMonitor {
    private NetworkStatsManager mNetworkStatsManager;

    private final List<Stats> mStatsList = new ArrayList<>();
    private Timer mTimer;
    private long mInterval;

    private OnTrafficListener mOnTrafficListener;

    public HostTrafficMonitor(Context context) {
        this(context, 30 * 1000);
    }

    public HostTrafficMonitor(Context context, long interval) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNetworkStatsManager = (NetworkStatsManager) context
                    .getSystemService(Context.NETWORK_STATS_SERVICE);
        }
        mInterval = interval;
    }

    public void setOnTrafficListener(OnTrafficListener listener) {
        mOnTrafficListener = listener;
    }

    /**
     * 通信量のモニタリングを開始します.
     */
    public void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                   monitoring();
                }
            }, 0, mInterval);
        }
    }

    /**
     * 通信量のモニタリングを停止します.
     */
    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 通信量の取得処理を行います.
     */
    private void monitoring() {
        Stats stats = getNetworkStats(0, System.currentTimeMillis());

        long rx = 0;
        long tx = 0;
        long bitrateRx = 0;
        long bitrateTx = 0;

        if (mStatsList.size() > 1) {
            Stats pre = mStatsList.get(mStatsList.size() - 1);
            rx = (stats.getTotalRxBytes() - pre.getTotalRxBytes());
            tx = (stats.getTotalTxBytes() - pre.getTotalTxBytes());
            bitrateRx = rx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
            bitrateTx = tx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
        }

        if (mOnTrafficListener != null) {
            mOnTrafficListener.onTraffic(rx, bitrateRx, tx, bitrateTx);
        }

        mStatsList.add(stats);

        if (mStatsList.size() > 100) {
            mStatsList.remove(0);
        }
    }

    public Stats getNetworkStats(long startTime, long endTime) {
        Stats stats = new Stats();

        stats.mStartTime = startTime;
        stats.mEndTime = endTime;
        try (NetworkStats result = mNetworkStatsManager.querySummary(
                ConnectivityManager.TYPE_WIFI, "", startTime, endTime)) {
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (result.hasNextBucket()) {
                result.getNextBucket(bucket);
                if (bucket.getUid() == android.os.Process.myUid()) {
                    stats.mTotalTxPackets += bucket.getTxPackets();
                    stats.mTotalRxPackets += bucket.getRxPackets();
                    stats.mTotalTxBytes += bucket.getTxBytes();
                    stats.mTotalRxBytes += bucket.getRxBytes();
                }
            }
        } catch (Exception e) {
            // ignore.
        }

        return stats;
    }

    public interface OnTrafficListener {
        void onTraffic(long rx, long bitrateRx, long tx, long bitrateTx);
    }

    public static class Stats {
        private long mStartTime;
        private long mEndTime;

        private long mTotalTxPackets;
        private long mTotalRxPackets;
        private long mTotalTxBytes;
        private long mTotalRxBytes;

        /**
         * 計測開始時間を取得します.
         *
         * @return 計測開始時間
         */
        public long getStartTime() {
            return mStartTime;
        }

        /**
         * 計測終了時間を取得します.
         *
         * @return 計測終了時間
         */
        public long getEndTime() {
            return mEndTime;
        }

        /**
         * 送信パケット数を取得します.
         *
         * @return 送信パケット数
         */
        public long getTotalTxPackets() {
            return mTotalTxPackets;
        }

        /**
         * 受信パケット数を取得します.
         *
         * @return 受信パケット数
         */
        public long getTotalRxPackets() {
            return mTotalRxPackets;
        }

        /**
         * 送信バイト数を取得します.
         *
         * @return 送信バイト数
         */
        public long getTotalTxBytes() {
            return mTotalTxBytes;
        }

        /**
         * 受信バイト数を取得します.
         *
         * @return 受信バイト数
         */
        public long getTotalRxBytes() {
            return mTotalRxBytes;
        }

        @Override
        public String toString() {
            return "totalTxPackets: " + mTotalTxPackets + "\n"
                    +  "totalRxPackets: " + mTotalRxPackets + "\n"
                    +  "totalTxBytes: " + mTotalTxBytes + "\n"
                    +  "totalRxBytes: " + mTotalRxBytes + "\n";
        }
    }
}
