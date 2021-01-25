package org.deviceconnect.android.deviceplugin.host.connection;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.M)
public class HostTrafficMonitor {
    /**
     * 通信量を計測するネットワークタイプのリスト.
     */
    private static final List<Integer> NETWORK_TYPE_LIST = Arrays.asList(
            ConnectivityManager.TYPE_MOBILE,
            ConnectivityManager.TYPE_WIFI);

    private NetworkStatsManager mNetworkStatsManager;

    private final Map<Integer, List<Stats>> mStatsMap = new HashMap<>();
    private final long mInterval;
    private Timer mTimer;

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

        for (int networkType : NETWORK_TYPE_LIST) {
            mStatsMap.put(networkType, new ArrayList<>());
        }
    }

    /**
     * 通信量を通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
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
     * ネットワークごとの通信量を取得します.
     *
     * @param networkType ネットワークタイプ
     * @return ネットワークごとの通信量
     */
    public Traffic getTraffic(int networkType) {
        List<Stats> statsList = mStatsMap.get(networkType);
        if (statsList != null && statsList.size() > 1) {
            Stats pre = statsList.get(statsList.size() - 2);
            Stats stats = statsList.get(statsList.size() - 1);
            long rx = (stats.getTotalRxBytes() - pre.getTotalRxBytes());
            long tx = (stats.getTotalTxBytes() - pre.getTotalTxBytes());
            long bitrateRx = 8 * rx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
            long bitrateTx = 8 * tx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
            Traffic traffic = new Traffic();
            traffic.mNetworkType = networkType;
            traffic.mRx = rx;
            traffic.mTx = tx;
            traffic.mBitrateRx = bitrateRx;
            traffic.mBitrateTx = bitrateTx;
            return traffic;
        }
        return null;
    }

    /**
     * 通信量の取得処理を行います.
     */
    private void monitoring() {
        List<Traffic> trafficList = new ArrayList<>();

        for (int networkType : NETWORK_TYPE_LIST) {
            Traffic traffic = getNetworkStats(networkType);
            if (traffic != null) {
                trafficList.add(traffic);
            }
        }

        if (mOnTrafficListener != null && !trafficList.isEmpty()) {
            mOnTrafficListener.onTraffic(trafficList);
        }
    }

    private Traffic getNetworkStats(int networkType) {
        Traffic traffic = null;

        List<Stats> statsList = mStatsMap.get(networkType);
        if (statsList == null) {
            statsList = new ArrayList<>();
            mStatsMap.put(networkType, statsList);
        }

        Stats stats = getNetworkStats(networkType, 0, System.currentTimeMillis());
        if (statsList.isEmpty()) {
            Stats oldStats = getNetworkStats(networkType, 0, System.currentTimeMillis() - mInterval);
            statsList.add(oldStats);
        }

        statsList.add(stats);

        if (statsList.size() > 100) {
            statsList.remove(0);
        }

        if (statsList.size() > 1) {
            traffic = getTraffic(networkType);
        }

        return traffic;
    }

    private Stats getNetworkStats(int networkType, long startTime, long endTime) {
        Stats stats = new Stats();

        stats.mStartTime = startTime;
        stats.mEndTime = endTime;

        try (NetworkStats result = mNetworkStatsManager.querySummary(
                networkType, "", startTime, endTime)) {
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

    public static class Traffic {
        int mNetworkType;
        long mRx;
        long mTx;
        long mBitrateRx;
        long mBitrateTx;

        public int getNetworkType() {
            return mNetworkType;
        }

        public long getRx() {
            return mRx;
        }

        public long getTx() {
            return mTx;
        }

        public long getBitrateRx() {
            return mBitrateRx;
        }

        public long getBitrateTx() {
            return mBitrateTx;
        }

        @Override
        public String toString() {
            return "networkType: " + mNetworkType + "\n"
                    +  "rx: " + mRx + "\n"
                    +  "tx: " + mTx + "\n"
                    +  "BitrateRx: " + mBitrateRx + "\n"
                    +  "BitrateTx: " + mBitrateTx + "\n";
        }
    }

    public interface OnTrafficListener {
        /**
         * 通信量を通知します.

         */
        void onTraffic(List<Traffic> trafficList);
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
         * 受信 bps を取得します.
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
