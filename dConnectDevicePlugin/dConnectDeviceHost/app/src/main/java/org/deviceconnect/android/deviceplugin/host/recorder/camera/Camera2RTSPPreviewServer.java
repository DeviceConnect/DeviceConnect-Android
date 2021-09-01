package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2RTSPPreviewServer extends AbstractRTSPPreviewServer {

    Camera2RTSPPreviewServer(Context context, Camera2Recorder recorder) {
        super(context, recorder);
        setPort(getStreamingSettings().getPort());
    }

    @Override
    protected VideoStream createVideoStream() {
        Camera2Recorder recorder = (Camera2Recorder) getRecorder();
        switch (getStreamingSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new CameraH264VideoStream(recorder, 5006);
            case H265:
                return new CameraH265VideoStream(recorder, 5006);
        }
    }
}
