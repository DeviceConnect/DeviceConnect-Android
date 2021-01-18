package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraH264VideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private final VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     *
     * @param camera2Recorder 操作するカメラのレコーダ.
     * @param port 送信先のポート番号
     */
    CameraH264VideoStream(Camera2Recorder camera2Recorder, int port) {
        mVideoEncoder = new CameraVideoEncoder(camera2Recorder);
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
