package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

class UvcSRTPreviewServer extends AbstractSRTPreviewServer {
    UvcSRTPreviewServer(final Context context, final UvcH264Recorder recorder, final int port) {
        super(context, recorder);
        setPort(port);
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
