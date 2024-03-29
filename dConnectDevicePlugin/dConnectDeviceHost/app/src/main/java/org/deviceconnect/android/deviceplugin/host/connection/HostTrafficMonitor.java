package org.deviceconnect.android.deviceplugin.host.connection;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.M)
class HostTrafficMonitor {
    /**
     * 通信量を計測するネットワークタイプのリスト.
     */
    private static final List<Integer> NETWORK_TYPE_LIST = Arrays.asList(
            ConnectivityManager.TYPE_MOBILE,
            ConnectivityManager.TYPE_WIFI);

    private final Context mContext;
    private final Map<Integer, List<Stats>> mStatsMap = new HashMap<>();
    private final long mInterval;
    private Timer mTimer;
    private int mCacheSize = 50;

    private NetworkStatsManager mNetworkStatsManager;
    private OnTrafficListener mOnTrafficListener;

    HostTrafficMonitor(Context context) {
        this(context, 30 * 1000);
    }

    HostTrafficMonitor(Context context, long interval) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNetworkStatsManager = (NetworkStatsManager) context
                    .getSystemService(Context.NETWORK_STATS_SERVICE);
        }
        mInterval = interval;
        mContext = context;

        for (int networkType : NETWORK_TYPE_LIST) {
            mStatsMap.put(networkType, new ArrayList<>());
        }
    }

    /**
     * 通信量の情報をキャッシュするサイズを設定します.
     *
     * @param cacheSize キャッシュサイズ
     */
    void setCacheSize(int cacheSize) {
        mCacheSize = cacheSize;
    }

    /**
     * 通信量を通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnTrafficListener(OnTrafficListener listener) {
        mOnTrafficListener = listener;
    }

    /**
     * 通信量のモニタリングを開始します.
     */
    void startTimer() {
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
    void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * Stats の情報を HostTraffic に変換します.
     *
     * @param networkType ネットワークタイプ
     * @param stats 通信情報
     * @param pre 通信情報
     * @return HostTraffic
     */
    private HostTraffic conv(int networkType, Stats stats, Stats pre) {
        long rx = (stats.getTotalRxBytes() - pre.getTotalRxBytes());
        long tx = (stats.getTotalTxBytes() - pre.getTotalTxBytes());
        long bitrateRx = 8 * rx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
        long bitrateTx = 8 * tx / ((stats.getEndTime() - pre.getEndTime()) / 1000);
        HostTraffic traffic = new HostTraffic();
        traffic.mNetworkType = networkType;
        traffic.mRx = rx;
        traffic.mTx = tx;
        traffic.mBitrateRx = bitrateRx;
        traffic.mBitrateTx = bitrateTx;
        traffic.mStartTime = stats.mEndTime - mInterval;
        traffic.mEndTime = stats.mEndTime;
        return traffic;
    }

    /**
     * 指定されたネットワークの最新の通信量を取得します.
     *
     * @param networkType ネットワークタイプ
     * @return 指定されたネットワークの最新の通信量
     */
    private HostTraffic getLastTraffic(int networkType) {
        List<Stats> statsList = mStatsMap.get(networkType);
        if (statsList != null && statsList.size() > 1) {
            Stats pre = statsList.get(statsList.size() - 2);
            Stats stats = statsList.get(statsList.size() - 1);
            return conv(networkType, stats, pre);
        }
        return null;
    }

    /**
     * 指定されたネットワークの通信量のリストを取得します.
     *
     * @param networkType ネットワークタイプ
     * @return 指定されたネットワークの通信量のリスト
     */
    List<HostTraffic> getTrafficList(int networkType) {
        List<HostTraffic> trafficList = new ArrayList<>();
        List<Stats> statsList = mStatsMap.get(networkType);
        for (int i = 1; i < statsList.size(); i++) {
            Stats pre = statsList.get(i - 1);
            Stats stats = statsList.get(i);
            trafficList.add(conv(networkType, stats, pre));
        }
        return trafficList;
    }

    /**
     * 各ネットワーク毎の最新の通信量をリストに格納して返却します.
     *
     * @return 各ネットワーク毎の最新の通信量
     */
    List<HostTraffic> getLastTrafficForAllNetwork() {
        List<HostTraffic> trafficList = new ArrayList<>();
        for (int networkType : NETWORK_TYPE_LIST) {
            HostTraffic traffic = getLastTraffic(networkType);
            if (traffic != null) {
                trafficList.add(traffic);
            }
        }
        return trafficList;
    }

    /**
     * 通信量の取得処理を行います.
     */
    private void monitoring() {
        List<HostTraffic> trafficList = new ArrayList<>();

        for (int networkType : NETWORK_TYPE_LIST) {
            HostTraffic traffic = getNetworkStats(networkType);
            if (traffic != null) {
                trafficList.add(traffic);
            }
        }

        if (mOnTrafficListener != null && !trafficList.isEmpty()) {
            mOnTrafficListener.onTraffic(trafficList);
        }
    }

    /**
     * 指定されたネットワークタイプの通信情報を取得します.
     *
     * @param networkType ネットワークタイプ
     * @return ネットワーク情報
     */
    private HostTraffic getNetworkStats(int networkType) {
        HostTraffic traffic = null;

        List<Stats> statsList = mStatsMap.get(networkType);
        if (statsList == null) {
            statsList = new ArrayList<>();
            mStatsMap.put(networkType, statsList);
        }

        // startTime から endTime の区間の情報を取得するような API だが実際には通信の総量が取得されるため
        // ここでは、前回の総量から現在の総量を引いて通信量を計測するようにしている。
        Stats stats = getNetworkStats(networkType, 0, System.currentTimeMillis());
        if (statsList.isEmpty()) {
            Stats oldStats = getNetworkStats(networkType, 0, System.currentTimeMillis() - mInterval);
            statsList.add(oldStats);
        }

        statsList.add(stats);

        if (statsList.size() > mCacheSize) {
            statsList.remove(0);
        }

        if (statsList.size() > 1) {
            traffic = getLastTraffic(networkType);
        }

        return traffic;
    }

    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getSubscriberId();
            }
        }
        return "";
    }

    private Stats getNetworkStats(int networkType, long startTime, long endTime) {
        Stats stats = new Stats();
        stats.mStartTime = startTime;
        stats.mEndTime = endTime;
        try {
            NetworkStats.Bucket bucket = mNetworkStatsManager.querySummaryForDevice(networkType,
                    getSubscriberId(mContext, networkType), startTime, endTime);
            if (bucket != null) {
                stats.mTotalTxPackets += bucket.getTxPackets();
                stats.mTotalRxPackets += bucket.getRxPackets();
                stats.mTotalTxBytes += bucket.getTxBytes();
                stats.mTotalRxBytes += bucket.getRxBytes();
            }
        } catch (Throwable t) {
            // ignore
        }
        return stats;
    }

    public interface OnTrafficListener {
        /**
         * 通信量を通知します.
         *
         * @param trafficList ネットワークごとの通信量を格納したリスト
         */
        void onTraffic(List<HostTraffic> trafficList);
    }

    private static class Stats {
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
