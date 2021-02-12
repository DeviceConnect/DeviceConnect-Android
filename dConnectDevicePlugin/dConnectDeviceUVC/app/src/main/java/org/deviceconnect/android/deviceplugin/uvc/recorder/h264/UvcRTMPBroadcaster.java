package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractRTMPBroadcaster;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

class UvcRTMPBroadcaster extends AbstractRTMPBroadcaster {

    public UvcRTMPBroadcaster(UvcH264Recorder recorder, String broadcastURI) {
        super(recorder, broadcastURI);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        return new UvcVideoEncoder((UvcH264Recorder) getRecorder());
    }
}
