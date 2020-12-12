package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;

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
    public void startBroadcaster(String broadcastURI, OnBroadcasterListener listener) {
        if (mBroadcaster != null) {
            return;
        }

        mBroadcaster = new Camera2RTMPBroadcaster(mRecorder, broadcastURI);
        mBroadcaster.setOnBroadcasterEventListener(new Broadcaster.OnBroadcasterEventListener() {
            @Override
            public void onStarted() {
                listener.onStarted();
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onError(Exception e) {
            }
        });
        mBroadcaster.start();
    }

    @Override
    public void stopBroadcaster() {
        if (mBroadcaster != null) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }
}
