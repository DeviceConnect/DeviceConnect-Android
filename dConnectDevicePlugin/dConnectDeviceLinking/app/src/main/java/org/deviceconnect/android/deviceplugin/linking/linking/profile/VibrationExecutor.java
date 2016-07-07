/*
 VibrationExecutor
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Support Class for Vibration Profile pattern parameter.
 *
 * @author NTT DOCOMO, INC.
 */
final class VibrationExecutor {

    public interface VibrationControllable {
        void changeVibration(boolean isOn, CompleteListener listener);
    }

    public interface CompleteListener {
        void onComplete();
    }

    private VibrationControllable mListener;
    private ScheduledExecutorService mPatternService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mLatestScheduledFuture;
    private Queue<Long> mPatternQueue = new ConcurrentLinkedQueue<Long>();
    private boolean mIsOn = true;
    private int mLastIdentifier = 0;

    public VibrationExecutor() {
    }

    public synchronized void setVibrationControllable(VibrationControllable controllable) {
        mListener = controllable;
    }

    public synchronized void start(long[] flashing) {
        cancelSchedule();
        updateQueue(flashing);
        final VibrationControllable listener = getVibrationControllable();
        if (listener == null) {
            return;
        }

        mLastIdentifier++;
        final int identifier = mLastIdentifier;
        //Turn on vibration at first.
        listener.changeVibration(true, new CompleteListener() {
            @Override
            public void onComplete() {
                if (!checkIdentifier(identifier)) {
                    return;
                }
                Long firstInterval = getNextInterval();
                //Start first waiting during vibrating.
                schedule(new Runnable() {
                    @Override
                    public void run() {
                        onFinishFirstVibration();
                    }
                }, firstInterval);
            }
        });
    }

    private synchronized void onFinishFirstVibration() {
        //Turn off vibration and start next waiting.
        setOn(false);
        Long interval = getNextInterval();
        controlVibration(isOn(), new Runnable() {
            @Override
            public void run() {
                next(this);
            }

        }, interval);
    }

    private synchronized void next(Runnable runnable) {
        final Long interval = getNextInterval();
        if (interval == null) {
            onFinish();
            return;
        }
        VibrationControllable listener = getVibrationControllable();
        if (listener == null) {
            onFinish();
            return;
        }
        toggleOnOff();
        controlVibration(isOn(), runnable, interval);
    }

    private synchronized void controlVibration(boolean isOn, final Runnable runnable, final Long interval) {
        mLastIdentifier++;
        final int identifier = mLastIdentifier;
        VibrationControllable listener = getVibrationControllable();
        if (listener == null) {
            onFinish();
            return;
        }
        listener.changeVibration(isOn, new CompleteListener() {
            @Override
            public void onComplete() {
                if (!checkIdentifier(identifier)) {
                    return;
                }
                if (isLastPattern()) {
                    onFinish();
                } else {
                    schedule(runnable, interval);
                }
            }
        });
    }

    private synchronized boolean isLastPattern() {
        return mPatternQueue.size() == 0;
    }

    private synchronized boolean checkIdentifier(int identifier) {
        return identifier == mLastIdentifier;
    }

    private synchronized void onFinish() {
        mListener = null;
        mLatestScheduledFuture = null;
    }

    private synchronized VibrationControllable getVibrationControllable() {
        return mListener;
    }

    private synchronized void cancelSchedule() {
        if (mLatestScheduledFuture != null && !mLatestScheduledFuture.isCancelled()) {
            mLatestScheduledFuture.cancel(false);
        }
    }

    private synchronized void schedule(Runnable runnable, long interval) {
        mLatestScheduledFuture = mPatternService.schedule(runnable, interval, TimeUnit.MILLISECONDS);
    }

    private synchronized void updateQueue(long[] flashing) {
        mPatternQueue.clear();
        for (long value : flashing) {
            mPatternQueue.add(value);
        }
    }

    private synchronized Long getNextInterval() {
        return mPatternQueue.poll();
    }

    private synchronized void setOn(boolean isOn) {
        mIsOn = isOn;
    }

    private synchronized boolean isOn() {
        return mIsOn;
    }

    private synchronized void toggleOnOff() {
        mIsOn = !mIsOn;
    }

}
