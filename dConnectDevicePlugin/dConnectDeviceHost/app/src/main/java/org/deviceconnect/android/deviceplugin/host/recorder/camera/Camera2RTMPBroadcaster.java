package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTMPBroadcaster;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class Camera2RTMPBroadcaster extends AbstractRTMPBroadcaster {

    public Camera2RTMPBroadcaster(Camera2Recorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        return new CameraVideoEncoder((Camera2Recorder) getRecorder());
    }
}
