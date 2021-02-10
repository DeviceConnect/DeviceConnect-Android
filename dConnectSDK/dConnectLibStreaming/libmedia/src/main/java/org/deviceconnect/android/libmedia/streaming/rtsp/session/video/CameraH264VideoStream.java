package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraH264VideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private CameraSurfaceVideoEncoder mVideoEncoder;

    /**
     * コンストラクタ.
     */
    public CameraH264VideoStream() {
        this(new CameraSurfaceVideoEncoder("video/avc"));
    }

    /**
     * コンストラクタ.
     */
    public CameraH264VideoStream(EGLSurfaceDrawingThread thread) {
        this(new CameraSurfaceVideoEncoder("video/avc", thread));
    }

    /**
     * コンストラクタ.
     *
     * @param encoder エンコーダ
     */
    public CameraH264VideoStream(CameraSurfaceVideoEncoder encoder) {
        super();
        mVideoEncoder = encoder;
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
