package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class ScreenCastH264VideoStream extends H264VideoStream {
    private ScreenCastVideoEncoder mVideoEncoder;

    ScreenCastH264VideoStream(ScreenCastRecorder recorder, int port) {
        mVideoEncoder = new ScreenCastVideoEncoder(recorder);
        setDestinationPort(port);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
