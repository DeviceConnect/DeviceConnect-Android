package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractSRTBroadcaster;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class UvcSRTBroadcaster extends AbstractSRTBroadcaster {

    public UvcSRTBroadcaster(UvcRecorder recorder, String broadcastURI) {
        super(recorder, broadcastURI);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        UvcRecorder recorder = (UvcRecorder) getRecorder();
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
