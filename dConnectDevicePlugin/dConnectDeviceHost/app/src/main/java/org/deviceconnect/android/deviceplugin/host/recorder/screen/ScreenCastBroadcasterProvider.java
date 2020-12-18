package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenCastBroadcasterProvider implements BroadcasterProvider {

    private ScreenCastRecorder mRecorder;
    private ScreenCastRTMPBroadcaster mBroadcaster;

    public ScreenCastBroadcasterProvider(ScreenCastRecorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public Broadcaster getBroadcaster() {
        return mBroadcaster;
    }

    @Override
    public Broadcaster startBroadcaster(String broadcastURI) {
        if (mBroadcaster != null) {
            return mBroadcaster;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);

        mBroadcaster = new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI);
        mBroadcaster.setOnBroadcasterEventListener(new Broadcaster.OnBroadcasterEventListener() {
            @Override
            public void onStarted() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onError(Exception e) {
                result.set(false);
                latch.countDown();
            }
        });
        mBroadcaster.start();

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }

        mBroadcaster.setOnBroadcasterEventListener(null);

        if (!result.get()) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }

        return mBroadcaster;
    }

    @Override
    public void stopBroadcaster() {
        if (mBroadcaster != null) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }
}
