package org.deviceconnect.android.deviceplugin.hitoe.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Hitoe Scheduler.
 *
 * @author NTT DOCOMO, INC.
 */

public class HitoeScheduler {

    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ScheduledFuture of scan timer.
     */
    private ScheduledFuture<?> mScanTimerFuture;

    /**
     * Defines a delay 1 second at first execution.
     */
    private static final long SCAN_FIRST_WAIT_PERIOD = 1000;

    /**
     * Defines a period 10 seconds between successive executions.
     */
    private static final long SCAN_WAIT_PERIOD = 10 * 1000;
    /**
     * Scanning flag.
     */
    private boolean mScanning;
    /** Notify listener. */
    private OnRegularNotify mNotify;
    /** Default wait period. */
    private long mWaitPeriod = SCAN_WAIT_PERIOD;
    /** Default first wait period. */
    private long mFirstWaitPeriod = SCAN_FIRST_WAIT_PERIOD;

    /**
     * Constructor.
     * @param notify listener
     * @param first first wait period
     * @param period wait period
     */
    public HitoeScheduler(final OnRegularNotify notify, final long first, final long period) {
        mNotify = notify;
        if (first > 0) {
            mFirstWaitPeriod = first;
        }
        if (period > 0) {
            mWaitPeriod = period;
        }
    }
    /**
     * Scan Hitoe device.
     * @param enable scan flag
     */
    public synchronized void scanHitoeDevice(final boolean enable) {
        if (enable) {
            if (mScanning || mScanTimerFuture != null) {
                // scan have already started.
                return;
            }
            mScanning = true;
            mScanTimerFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (mNotify != null) {
                        mNotify.onRegularNotify();
                    }
                }
            }, mFirstWaitPeriod, mWaitPeriod, TimeUnit.MILLISECONDS);
        } else {
            mScanning = false;
            cancelScanTimer();
        }
    }

    /**
     * Stopped the scan timer.
     */
    private synchronized void cancelScanTimer() {
        if (mScanTimerFuture != null) {
            mScanTimerFuture.cancel(true);
            mScanTimerFuture = null;
        }
    }

    public interface OnRegularNotify {
        void onRegularNotify();
    }
}
