package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class ScreenCastBroadcasterProvider extends AbstractBroadcastProvider {

    private ScreenCastRecorder mRecorder;

    public ScreenCastBroadcasterProvider(ScreenCastRecorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        return new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI);
    }
}
