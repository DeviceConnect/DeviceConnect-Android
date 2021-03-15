package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class UvcH264VideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private final VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     * コンストラクタ.
     *
     * @param recorder 操作するカメラのレコーダ.
     * @param port 送信先のポート番号
     */
    UvcH264VideoStream(UvcRecorder recorder, int port) {
        mVideoEncoder = new UvcVideoEncoder(recorder);
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
