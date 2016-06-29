/*
 TimeoutSchedule.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class TimeoutSchedule implements Runnable {
    private static final int TIMEOUT = 30;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;

    protected LinkingBeacon mBeacon;
    protected boolean mCleanupFlag;

    TimeoutSchedule(final LinkingBeacon beacon) {
        this(beacon, TIMEOUT);
    }

    TimeoutSchedule(final LinkingBeacon beacon, final long delay) {
        mBeacon = beacon;
        mScheduledFuture = mExecutorService.schedule(this, delay, TimeUnit.SECONDS);
    }

    protected void cleanup() {
        if (mCleanupFlag) {
            return;
        }
        mCleanupFlag = true;

        onCleanup();

        mScheduledFuture.cancel(false);
        mExecutorService.shutdown();
    }

    @Override
    public synchronized void run() {
        onTimeout();
        cleanup();
    }

    public abstract void onCleanup();
    public abstract void onTimeout();
}
