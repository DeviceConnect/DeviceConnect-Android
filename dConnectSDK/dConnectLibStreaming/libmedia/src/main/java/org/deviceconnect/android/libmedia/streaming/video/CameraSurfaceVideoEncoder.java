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
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 映像のエンコード設定.
     */
    private CameraVideoQuality mVideoQuality;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraSurfaceVideoEncoder(Context context) {
        this(context, "video/avc");
    }

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraSurfaceVideoEncoder(Context context, EGLSurfaceDrawingThread thread) {
        this(context, "video/avc", thread);
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param mimeType MediaCodec に渡すマイムタイプ
     */
    public CameraSurfaceVideoEncoder(Context context, String mimeType) {
        this(context, new CameraVideoQuality(mimeType));
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param mimeType MediaCodec に渡すマイムタイプ
     */
    public CameraSurfaceVideoEncoder(Context context, String mimeType, EGLSurfaceDrawingThread thread) {
        this(context, new CameraVideoQuality(mimeType), thread);
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param videoQuality 映像エンコードの設定
     */
    public CameraSurfaceVideoEncoder(Context context, CameraVideoQuality videoQuality) {
        this(context, videoQuality, null);
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param videoQuality 映像エンコードの設定
     */
    public CameraSurfaceVideoEncoder(Context context, CameraVideoQuality videoQuality, EGLSurfaceDrawingThread thread) {
        super(thread);
        mContext = context;
        mVideoQuality = videoQuality;
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    @Override
    protected int getDisplayRotation() {
        return Camera2WrapperManager.getDisplayRotation(mContext);
    }

    @Override
    public boolean isSwappedDimensions() {
        return Camera2WrapperManager.isSwappedDimensions(mContext, mVideoQuality.getFacing());
    }

    // SurfaceVideoEncoder

    @Override
    protected void onStartSurfaceDrawing() {
    }

    @Override
    protected void onStopSurfaceDrawing() {
    }
}
