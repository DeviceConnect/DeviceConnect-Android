package org.deviceconnect.android.libsrt.server.video;


import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.server.MediaStream;

public abstract class VideoStream extends MediaStream {
    public abstract VideoEncoder getVideoEncoder();
}
