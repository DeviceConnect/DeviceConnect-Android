package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H265VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class ScreenCastH265VideoStream extends H265VideoStream {
    private final ScreenCastVideoEncoder mVideoEncoder;

    ScreenCastH265VideoStream(ScreenCastRecorder recorder, int port) {
        mVideoEncoder = new ScreenCastVideoEncoder(recorder, "video/hevc");
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
