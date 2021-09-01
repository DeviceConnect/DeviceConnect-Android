package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class Camera2SRTPreviewServer extends AbstractSRTPreviewServer {
    Camera2SRTPreviewServer(final Context context, final Camera2Recorder recorder) {
        super(context, recorder);
        setPort(getStreamingSettings().getPort());
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
