package org.deviceconnect.android.libmedia.streaming.video;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class CameraSurfaceVideoEncoder extends SurfaceVideoEncoder {
    /**
     * 映像のエンコード設定.
     */
    private final CameraVideoQuality mVideoQuality;

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
     * @param thread 描画用スレッド
     */
    public CameraSurfaceVideoEncoder(EGLSurfaceDrawingThread thread) {
        this("video/avc", thread);
    }

    /**
     * コンストラクタ.
     *
     * @param mimeType MediaCodec に渡すマイムタイプ
     * @param thread 描画用スレッド
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
     * @param thread 描画用スレッド
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
}
