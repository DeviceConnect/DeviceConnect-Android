package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractSRTBroadcaster;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

class UvcSRTBroadcaster extends AbstractSRTBroadcaster {

    public UvcSRTBroadcaster(UvcH264Recorder recorder, String broadcastURI) {
        super(recorder, broadcastURI);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        UvcH264Recorder recorder = (UvcH264Recorder) getRecorder();
        MediaRecorder.Settings settings = recorder.getSettings();
        switch (settings.getPreviewEncoderName()) {
            case H264:
            default:
                return new UvcVideoEncoder(recorder);
            case H265:
                return new UvcVideoEncoder(recorder, "video/hevc");
        }
    }
}
