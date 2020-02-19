package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.io.IOException;

public class CameraVideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     *
     * @param camera2Recorder 操作するカメラのレコーダ.
     * @param port 送信先のポート番号
     */
    CameraVideoStream(Camera2Recorder camera2Recorder, int port) {
        mVideoEncoder = new CameraVideoEncoder(camera2Recorder) {
            @Override
            protected void prepare() throws IOException {
                prepareVideoEncoder();
                super.prepare();
            }
        };
        setDestinationPort(port);
    }

    /**
     * VideoEncoder#prepare() の前に処理を行います.
     * <p>
     * ここで、VideoEncoder の設定などを行ってください。
     * </p>
     */
    void prepareVideoEncoder() {

    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
