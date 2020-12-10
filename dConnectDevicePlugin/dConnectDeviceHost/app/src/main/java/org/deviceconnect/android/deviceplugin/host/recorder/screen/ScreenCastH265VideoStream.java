package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class ScreenCastH265VideoStream extends H264VideoStream {
    private ScreenCastVideoEncoder mVideoEncoder;

    ScreenCastH265VideoStream(ScreenCastManager screenCastManager, int port) {
        mVideoEncoder = new ScreenCastVideoEncoder(screenCastManager, "video/hevc");
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
