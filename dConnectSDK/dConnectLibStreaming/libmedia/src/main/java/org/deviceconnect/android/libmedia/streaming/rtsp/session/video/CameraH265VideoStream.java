package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraH265VideoStream extends H265VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private CameraSurfaceVideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     */
    public CameraH265VideoStream() {
        this(new CameraSurfaceVideoEncoder("video/hevc"));
    }

    /**
     * コンストラクタ.
     */
    public CameraH265VideoStream(EGLSurfaceDrawingThread thread) {
        this(new CameraSurfaceVideoEncoder("video/hevc", thread));
    }

    /**
     * コンストラクタ.
     *
     * @param encoder コンテキスト
     */
    public CameraH265VideoStream(CameraSurfaceVideoEncoder encoder) {
        super();
        mVideoEncoder = encoder;
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
