/*
 TimeoutSchedule.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class TimeoutSchedule implements Runnable, LinkingBeaconManager.OnBeaconScanStateListener {
    private static final int TIMEOUT = 30 * 1000;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;

    protected LinkingBeaconManager mBeaconManager;
    protected LinkingBeacon mBeacon;
    protected boolean mCleanupFlag;

    TimeoutSchedule(final LinkingBeaconManager manager, final LinkingBeacon beacon) {
        this(manager, beacon, TIMEOUT);
    }

    TimeoutSchedule(final LinkingBeaconManager manager, final LinkingBeacon beacon, final long delay) {
        mBeaconManager = manager;
        mBeacon = beacon;
        mScheduledFuture = mExecutorService.schedule(this, delay, TimeUnit.MILLISECONDS);
        mBeaconManager.addOnBeaconScanStateListener(this);
    }

    protected void cleanup() {
        if (mCleanupFlag) {
            return;
        }
        mCleanupFlag = true;

        onCleanup();
        mBeaconManager.removeOnBeaconScanStateListener(this);

        mScheduledFuture.cancel(false);
        mExecutorService.shutdown();
    }

    @Override
    public synchronized void run() {
        onTimeout();
        cleanup();
    }

    @Override
    public synchronized void onScanState(final LinkingBeaconUtil.ScanState state, final LinkingBeaconUtil.ScanDetail detail) {
        if (state != LinkingBeaconUtil.ScanState.RESULT_OK || detail != LinkingBeaconUtil.ScanDetail.DETAIL_OK) {
            String message;
            switch (detail) {
                case DETAIL_TIMEOUT:
                    message = mBeaconManager.getContext().getString(R.string.linking_beacon_scan_detail_timeout);
                    break;
                case DETAIL_META_DATA_NONE:
                    message = mBeaconManager.getContext().getString(R.string.linking_beacon_scan_detail_meta_data_none);
                    break;
                case DETAIL_BT_DISABLED:
                    message = mBeaconManager.getContext().getString(R.string.linking_beacon_scan_detail_bt_disabled);
                    break;
                case DETAIL_SDA_DISABLED:
                    message = mBeaconManager.getContext().getString(R.string.linking_beacon_scan_detail_sda_disabled);
                    break;
                case DETAIL_PERMISSION_DENIED:
                    message = mBeaconManager.getContext().getString(R.string.linking_beacon_scan_detail_permission_denied);
                    break;
                default:
                    message = "Unknown";
                    break;
            }
            onDisableScan(message);
            cleanup();
        }
    }

    public abstract void onDisableScan(String message);
    public abstract void onCleanup();
    public abstract void onTimeout();
}
