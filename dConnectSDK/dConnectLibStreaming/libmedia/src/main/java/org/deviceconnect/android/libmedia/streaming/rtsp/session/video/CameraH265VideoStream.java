package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import android.content.Context;
import android.view.Surface;

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
     *
     * @param context コンテキスト
     */
    public CameraH265VideoStream(Context context) {
        this(new CameraSurfaceVideoEncoder(context, "video/hevc"));
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CameraH265VideoStream(Context context, EGLSurfaceDrawingThread thread) {
        this(new CameraSurfaceVideoEncoder(context, "video/hevc", thread));
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
