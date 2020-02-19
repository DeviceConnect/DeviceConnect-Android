package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.io.IOException;

public class ScreenCastVideoStream extends H264VideoStream {
    private ScreenCastVideoEncoder mVideoEncoder;

    ScreenCastVideoStream(ScreenCastManager screenCastManager, int port) {
        mVideoEncoder = new ScreenCastVideoEncoder(screenCastManager) {
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
