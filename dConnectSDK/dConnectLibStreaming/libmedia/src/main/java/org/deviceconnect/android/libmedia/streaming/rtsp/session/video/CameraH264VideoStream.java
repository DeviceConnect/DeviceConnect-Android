package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import android.content.Context;
import android.view.Surface;

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
     *
     * @param context コンテキスト
     */
    public CameraH264VideoStream(Context context) {
        this(new CameraSurfaceVideoEncoder(context));
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CameraH264VideoStream(Context context, EGLSurfaceDrawingThread thread) {
        this(new CameraSurfaceVideoEncoder(context, "video/avc", thread));
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
