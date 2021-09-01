package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTBroadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class Camera2SRTBroadcaster extends AbstractSRTBroadcaster {

    public Camera2SRTBroadcaster(Camera2Recorder recorder, String broadcastURI, String name) {
        super(recorder, broadcastURI, name);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        Camera2Recorder recorder = (Camera2Recorder) getRecorder();
        switch (getStreamingSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new CameraVideoEncoder(recorder);
            case H265:
                return new CameraVideoEncoder(recorder, "video/hevc");
        }
    }
}
