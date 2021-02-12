package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class UvcSRTPreviewServer extends AbstractSRTPreviewServer {
    UvcSRTPreviewServer(final Context context, final UvcRecorder recorder, final int port) {
        super(context, recorder);
        setPort(port);
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
