package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class TimeoutSchedule implements Runnable {
    private static final int TIMEOUT = 30;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;

    protected LinkingDevice mDevice;
    protected boolean mCleanupFlag;

    TimeoutSchedule(final LinkingDevice device) {
        this(device, TIMEOUT);
    }

    TimeoutSchedule(final LinkingDevice device, final long delay) {
        mDevice = device;
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
