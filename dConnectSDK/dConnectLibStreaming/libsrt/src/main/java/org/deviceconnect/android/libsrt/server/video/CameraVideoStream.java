package org.deviceconnect.android.libsrt.server.video;

import android.content.Context;

import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;


public class CameraVideoStream extends VideoStream {

    private CameraSurfaceVideoEncoder mVideoSurfaceEncoder;

    public CameraVideoStream(Context context) {
        mVideoSurfaceEncoder = new CameraSurfaceVideoEncoder(context);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoSurfaceEncoder;
    }
}
