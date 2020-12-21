package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Camera2BroadcasterProvider implements BroadcasterProvider {

    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    /**
     * カメラのプレビューを配信するクラス.
     */
    private Camera2RTMPBroadcaster mBroadcaster;

    public Camera2BroadcasterProvider(Camera2Recorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public Broadcaster getBroadcaster() {
        return mBroadcaster;
    }

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }

    @Override
    public Broadcaster startBroadcaster(String broadcastURI) {
        if (mBroadcaster != null && mBroadcaster.isRunning()) {
            return mBroadcaster;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);

        mBroadcaster = new Camera2RTMPBroadcaster(mRecorder, broadcastURI);
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
    public void onConfigChange() {
        if (mBroadcaster != null) {
            mBroadcaster.onConfigChange();
        }
    }
}
