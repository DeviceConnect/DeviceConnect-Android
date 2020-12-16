package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H265VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraH265VideoStream extends H265VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     *
     * @param camera2Recorder 操作するカメラのレコーダ.
     * @param port            送信先のポート番号
     */
    CameraH265VideoStream(Camera2Recorder camera2Recorder, int port) {
        mVideoEncoder = new CameraVideoEncoder(camera2Recorder, "video/hevc");
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}