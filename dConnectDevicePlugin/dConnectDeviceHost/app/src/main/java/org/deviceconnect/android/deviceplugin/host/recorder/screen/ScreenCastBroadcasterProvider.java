package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;

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
    public void startBroadcaster(String broadcastURI, OnBroadcasterListener listener) {
        if (mBroadcaster != null) {
            return;
        }

        mBroadcaster = new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI);
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

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }
}
