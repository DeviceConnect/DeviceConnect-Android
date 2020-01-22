package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraVideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private VideoEncoder mVideoEncoder;

    CameraVideoStream(Camera2Recorder camera2Recorder) {
        mVideoEncoder = new CameraVideoEncoder(camera2Recorder);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
