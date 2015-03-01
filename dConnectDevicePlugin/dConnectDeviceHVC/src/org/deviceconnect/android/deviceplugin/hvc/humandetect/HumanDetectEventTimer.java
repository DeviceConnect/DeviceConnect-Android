package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Human Detect Event Timer.
 */
public class HumanDetectEventTimer extends Timer {
    
    /**
     * timer running flag.
     */
    private boolean mIsTimerRunning = false;
    
    /**
     * minute per msec.
     */
    private static final long MINUTE_PER_MSEC = 60 * 1000;
    
    /**
     * timer interval time[msec].
     */
    private static final long TIMER_INTERVAL = 1 * MINUTE_PER_MSEC;
    
    /**
     * timer process listener.
     */
    private HumanDetectEventTimerListener mListener;
    
    /**
     * start timer.
     * @param listener timer listener
     */
    public void startTimer(final HumanDetectEventTimerListener listener) {
        mListener = listener;
        synchronized (this) {
            if (!mIsTimerRunning) {
                scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onTimer();
                        }
                    }
                }, TIMER_INTERVAL, TIMER_INTERVAL);
                mIsTimerRunning = true;
            }
        }
    }
    
    /**
     * stop timer.
     */
    public void stopTimer() {
        synchronized (this) {
            if (mIsTimerRunning) {
                cancel();
                mIsTimerRunning = false;
            }
        }
    }

    /**
     * check timer running.
     * @return true : running / false: not running.
     */
    public boolean isTimerRunning() {
        synchronized (this) {
            return mIsTimerRunning;
        }
    }
    
}
