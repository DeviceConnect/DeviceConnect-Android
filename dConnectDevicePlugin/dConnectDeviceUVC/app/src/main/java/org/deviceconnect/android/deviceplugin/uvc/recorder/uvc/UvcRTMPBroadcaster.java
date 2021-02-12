package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractRTMPBroadcaster;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class UvcRTMPBroadcaster extends AbstractRTMPBroadcaster {

    public UvcRTMPBroadcaster(UvcRecorder recorder, String broadcastURI) {
        super(recorder, broadcastURI);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        return new UvcVideoEncoder((UvcRecorder) getRecorder());
    }
}
