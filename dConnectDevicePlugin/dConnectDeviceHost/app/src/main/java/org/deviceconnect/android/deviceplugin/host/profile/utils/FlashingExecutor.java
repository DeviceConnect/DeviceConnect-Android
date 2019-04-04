/*
 FlashingExecutor
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Support Class for Light Profile flashing parameter.
 *
 * @author NTT DOCOMO, INC.
 */
public class FlashingExecutor {

    public interface LightControllable {
        void changeLight(boolean isOn, LightControlCallback listener);
    }

    public interface LightControlCallback {

        /**
         * ライト状態を変更するまでに経過した時間
         * @param delay 経過時間
         */
        void onComplete(final long delay);

        void onFatalError();
    }

    private LightControllable mListener;
    private ScheduledExecutorService mFlashingService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mLatestScheduledFuture;
    private Queue<Long> mFlashingQueue = new ConcurrentLinkedQueue<>();
    private boolean mIsOn = true;
    private int mLastIdentifier = 0;

    public FlashingExecutor() {
    }

    public synchronized void setLightControllable(LightControllable controllable) {
        mListener = controllable;
    }

    public synchronized void start(long[] flashing) {
        cancelSchedule();
        updateQueue(flashing);
        resetOn();
        schedule(new Runnable() {
            @Override
            public void run() {
                controlLight(this);
            }
        }, getNextFlashingInterval());
    }

    private synchronized void controlLight(final Runnable runnable) {
        if (mFlashingQueue.isEmpty()) {
            onFinish();
        }
        mLastIdentifier++;
        final int identifier = mLastIdentifier;
        LightControllable listener = getLightControllable();
        if (listener == null) {
            next(runnable, identifier, 0);
            return;
        }
        listener.changeLight(isOn(), new LightControlCallback() {
            @Override
            public void onComplete(final long delay) {
                next(runnable, identifier, delay);
            }

            @Override
            public void onFatalError() {
                cancelSchedule();
            }
        });
    }

    private synchronized void next(final Runnable runnable, final int identifier, final long delay) {
        //Return if other execution has been begin.
        if (mLastIdentifier != identifier) {
            return;
        }
        toggleOnOff();
        Long interval = getNextFlashingInterval();
        if (interval != null) {
            long fixedInterval = interval - delay;
            if (fixedInterval < 0) {
                fixedInterval = 0;
            }
            schedule(runnable, fixedInterval);
        } else {
            onFinish();
        }
    }

    private synchronized void onFinish() {
        mListener = null;
        mLatestScheduledFuture = null;
    }

    private synchronized LightControllable getLightControllable() {
        return mListener;
    }

    private synchronized void cancelSchedule() {
        if (mLatestScheduledFuture != null && !mLatestScheduledFuture.isCancelled()) {
            mLatestScheduledFuture.cancel(false);
        }
    }

    private synchronized void schedule(Runnable runnable, long interval) {
        mLatestScheduledFuture = mFlashingService.schedule(runnable, interval, TimeUnit.MILLISECONDS);
    }

    private synchronized void updateQueue(long[] flashing) {
        mFlashingQueue.clear();
        mFlashingQueue.add(0L);
        for (long value : flashing) {
            mFlashingQueue.add(value);
        }
    }

    private synchronized Long getNextFlashingInterval() {
        return mFlashingQueue.poll();
    }

    private synchronized void resetOn() {
        mIsOn = true;
    }

    private synchronized boolean isOn() {
        return mIsOn;
    }

    private synchronized void toggleOnOff() {
        mIsOn = !mIsOn;
    }

}
