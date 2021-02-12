package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

class UvcH264VideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private final VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     *
     * @param recorder 操作するカメラのレコーダ.
     * @param port 送信先のポート番号
     */
    UvcH264VideoStream(UvcH264Recorder recorder, int port) {
        mVideoEncoder = new UvcVideoEncoder(recorder);
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
