package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H265VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class UvcH265VideoStream extends H265VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private final VideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     *
     * @param recorder 操作するカメラのレコーダ.
     * @param port            送信先のポート番号
     */
    UvcH265VideoStream(UvcRecorder recorder, int port) {
        mVideoEncoder = new UvcVideoEncoder(recorder, "video/hevc");
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}