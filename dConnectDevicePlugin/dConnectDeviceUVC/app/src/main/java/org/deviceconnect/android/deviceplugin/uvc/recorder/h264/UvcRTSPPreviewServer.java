package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

class UvcRTSPPreviewServer extends AbstractRTSPPreviewServer {

    UvcRTSPPreviewServer(Context context, UvcH264Recorder recorder, int port) {
        super(context, recorder);
        setPort(port);
    }

    @Override
    protected VideoStream createVideoStream() {
        UvcH264Recorder recorder = (UvcH264Recorder) getRecorder();
        MediaRecorder.Settings settings = recorder.getSettings();
        switch (settings.getPreviewEncoderName()) {
            case H264:
            default:
                return new UvcH264VideoStream(recorder, 5006);
            case H265:
                return new UvcH265VideoStream(recorder, 5006);
        }
    }
}
