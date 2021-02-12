package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

public class UvcRTSPPreviewServer extends AbstractRTSPPreviewServer {

    UvcRTSPPreviewServer(Context context, UvcRecorder recorder, int port) {
        super(context, recorder);
        setPort(port);
    }

    @Override
    protected VideoStream createVideoStream() {
        UvcRecorder recorder = (UvcRecorder) getRecorder();
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
