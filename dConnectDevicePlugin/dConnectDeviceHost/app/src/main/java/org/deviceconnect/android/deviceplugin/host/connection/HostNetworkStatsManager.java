package org.deviceconnect.android.deviceplugin.host.connection;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;

public class HostNetworkStatsManager {
    private NetworkStatsManager mNetworkStatsManager;

    public HostNetworkStatsManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNetworkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Stats getNetworkStats(long startTime, long endTime) {
        Stats stats = new Stats();

//        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        String subscriberID = tm.getSubscriberId();
//        NetworkStats networkStatsByApp = networkStatsManager.queryDetailsForUid(
//              ConnectivityManager.TYPE_MOBILE, subscriberID, start, end, uid);

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
        } catch (RemoteException | SecurityException e) {
            throw new RuntimeException(e);
        }
        return stats;
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

        /**
         * データ送信のビットレートを取得します.
         *
         * @return データ送信のビットレート
         */
        public long getTxBitRate() {
            return 8 * mTotalTxBytes / ((mEndTime - mStartTime) / 1000);
        }

        /**
         * データ受信のビットレートを取得します.
         *
         * @return データ受信のビットレート
         */
        public long getRxBitRate() {
            return 8 * mTotalRxBytes / ((mEndTime - mStartTime) / 1000);
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
