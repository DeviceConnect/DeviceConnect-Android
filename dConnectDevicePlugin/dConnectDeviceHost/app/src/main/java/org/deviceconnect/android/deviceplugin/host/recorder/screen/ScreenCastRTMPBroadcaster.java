package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTMPBroadcaster;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class ScreenCastRTMPBroadcaster extends AbstractRTMPBroadcaster {
    public ScreenCastRTMPBroadcaster(ScreenCastRecorder recorder, String broadcastURI, String name) {
        super(recorder, broadcastURI, name);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        return new ScreenCastVideoEncoder((ScreenCastRecorder) getRecorder());
    }
}
