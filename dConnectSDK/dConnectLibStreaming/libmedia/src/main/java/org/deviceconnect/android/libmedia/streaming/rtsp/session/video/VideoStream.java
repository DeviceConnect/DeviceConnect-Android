package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.MediaStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public abstract class VideoStream extends MediaStream {
    /**
     * 映像を配信するポート番号を定義します.
     */
    private static final int VIDEO_PORT = 5006;

    public VideoStream() {
        setDestinationPort(VIDEO_PORT);
    }

    /**
     * 映像用のエンコーダを取得します.
     *
     * @return 映像用のエンコーダ
     */
    public abstract VideoEncoder getVideoEncoder();
}
