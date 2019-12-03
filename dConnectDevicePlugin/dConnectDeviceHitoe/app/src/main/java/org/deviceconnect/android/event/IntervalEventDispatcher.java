package org.deviceconnect.android.event;

import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IntervalEventDispatcher extends EventDispatcher {

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;

    private int mFirstPeriodTime;
    private int mPeriodTime;

    private Event mEvent;
    private Intent mMessage;

    private final Object mLockObject = new Object();

    public IntervalEventDispatcher(final DConnectMessageService service, final int firstPeriodTime, final int periodTime) {
        super(service);

        if (firstPeriodTime < 0) {
            throw new IllegalArgumentException("firstPeriodTime is negative.");
        }

        if (periodTime <= 0) {
            throw new IllegalArgumentException("periodTime is zero or negative.");
        }

        mFirstPeriodTime = firstPeriodTime;
        mPeriodTime = periodTime;
    }

    @Override
    public void sendEvent(final Event event, final Intent message) {
        synchronized (mLockObject) {
            mEvent = event;
            mMessage = message;
        }
    }

    @Override
    public void start() {
        if (mScheduledFuture != null) {
            throw new IllegalStateException("This IntervalEventDispatcher already started.");
        }
        mScheduledFuture = mExecutorService.scheduleAtFixedRate(mRunnable, mFirstPeriodTime, mPeriodTime,TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(true);
            mExecutorService.shutdown();
        }
    }

    private Runnable mRunnable = () -> {
        synchronized (mLockObject) {
            if (mEvent != null && mMessage != null) {
                sendEventInternal(mEvent, mMessage);
            }
            mEvent = null;
            mMessage = null;
        }
    };
}
