package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTBroadcaster;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class ScreenCastSRTBroadcaster extends AbstractSRTBroadcaster {

    public ScreenCastSRTBroadcaster(ScreenCastRecorder recorder, String name) {
        super(recorder, name);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        switch (getEncoderSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new ScreenCastVideoEncoder(recorder);
            case H265:
                return new ScreenCastVideoEncoder(recorder, "video/hevc");
        }
    }
}
