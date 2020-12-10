package org.deviceconnect.android.libmedia.streaming.video;

import android.content.Context;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CameraSurfaceVideoEncoder extends SurfaceVideoEncoder {
    /**
     * 映像のエンコード設定.
     */
    private CameraVideoQuality mVideoQuality;

    /**
     * コンストラクタ.
     *
     * @param mimeType MediaCodec に渡すマイムタイプ
     */
    public CameraSurfaceVideoEncoder(String mimeType) {
        this(new CameraVideoQuality(mimeType));
    }

    /**
     * コンストラクタ.
     *
     * @param mimeType MediaCodec に渡すマイムタイプ
     */
    public CameraSurfaceVideoEncoder(String mimeType, EGLSurfaceDrawingThread thread) {
        this(new CameraVideoQuality(mimeType), thread);
    }

    /**
     * コンストラクタ.
     *
     * @param videoQuality 映像エンコードの設定
     */
    public CameraSurfaceVideoEncoder(CameraVideoQuality videoQuality) {
        this(videoQuality, null);
    }

    /**
     * コンストラクタ.
     *
     * @param videoQuality 映像エンコードの設定
     */
    public CameraSurfaceVideoEncoder(CameraVideoQuality videoQuality, EGLSurfaceDrawingThread thread) {
        super(thread);
        mVideoQuality = videoQuality;
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    // SurfaceVideoEncoder

    @Override
    protected void onStartSurfaceDrawing() {
    }

    @Override
    protected void onStopSurfaceDrawing() {
    }
}
