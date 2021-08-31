package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.io.IOException;

/**
 * Surface に描画を行い、MediaCodec でエンコードするためのエンコーダ.
 */
public abstract class SurfaceVideoEncoder extends VideoEncoder {
    /**
     * Surface の描画を行うスレッド.
     */
    private EGLSurfaceDrawingThread mSurfaceDrawingThread;

    /**
     * MediaCodec の入力用 Surface.
     */
    private Surface mMediaCodecSurface;

    /**
     * このフラグが true の場合には、外部から EGLSurfaceDrawingThread が設定されたことを意味します。
     */
    private final boolean mInternalCreateSurfaceDrawingThread;

    /**
     * 描画先の EGLSurfaceBase.
     */
    private EGLSurfaceBase mEGLSurfaceBase;

    /**
     * EGLSurfaceDrawingThread からのイベントを受け取るためのリスナー.
     */
    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            onStartSurfaceDrawing();
        }

        @Override
        public void onStopped() {
            onStopSurfaceDrawing();
        }

        @Override
        public void onError(Exception e) {
            postOnError(new MediaEncoderException(e));
        }

        @Override
        public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
            if (mEGLSurfaceBase == eglSurfaceBase) {
                VideoQuality videoQuality = getVideoQuality();
                if (videoQuality != null) {
                    eglSurfaceBase.setDrawingRange(videoQuality.getCropRect());
                }
                onDrawnSurface();
            }
        }
    };

    public SurfaceVideoEncoder() {
        mInternalCreateSurfaceDrawingThread = true;
    }

    public SurfaceVideoEncoder(EGLSurfaceDrawingThread thread) {
        mSurfaceDrawingThread = thread;
        mInternalCreateSurfaceDrawingThread = false;
    }

    /**
     * 描画先の EGLSurfaceBase を取得します.
     *
     * エンコードが開始されていない場合には null を返却します。
     *
     * @return 描画先の EGLSurfaceBase
     */
    public EGLSurfaceBase getEGLSurfaceBase() {
        return mEGLSurfaceBase;
    }

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();
        mMediaCodecSurface = mMediaCodec.createInputSurface();
    }

    @Override
    protected void startRecording() {
        startDrawingThreadInternal();
    }

    @Override
    protected void stopRecording() {
        stopDrawingThreadInternal();
    }

    @Override
    protected void release() {
        mMediaCodecSurface = null;
        super.release();
    }

    // VideoEncoder

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    // private method.

    /**
     * Surface への描画スレッドを開始します。
     */
    private void startDrawingThreadInternal() {
        VideoQuality quality = getVideoQuality();

        if (mInternalCreateSurfaceDrawingThread) {
            mSurfaceDrawingThread = createEGLSurfaceDrawingThread();
        }

        mSurfaceDrawingThread.setSize(quality.getVideoWidth(), quality.getVideoHeight());
        mSurfaceDrawingThread.addEGLSurfaceBase(mMediaCodecSurface, quality.getCropRect());
        mSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
        mSurfaceDrawingThread.start();

        mEGLSurfaceBase = mSurfaceDrawingThread.findEGLSurfaceBaseByTag(mMediaCodecSurface);
    }

    /**
     * Surface への描画スレッドを停止します。
     */
    private void stopDrawingThreadInternal() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.removeEGLSurfaceBase(mMediaCodecSurface);
            mSurfaceDrawingThread.stop(false);
            mSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            if (mInternalCreateSurfaceDrawingThread) {
                mSurfaceDrawingThread = null;
            }
            mEGLSurfaceBase = null;
        }
    }

    /**
     * 内部で EGLSurfaceDrawingThread を作成します.
     *
     * 別の EGLSurfaceDrawingThread を使用した場合には、このメソッドをオーバーライドしてください。
     *
     * @return EGLSurfaceDrawingThread のインスタンス
     */
    protected EGLSurfaceDrawingThread createEGLSurfaceDrawingThread() {
        return new EGLSurfaceDrawingThread();
    }

    /**
     * 描画を行う SurfaceTexture を取得します.
     *
     * <p>
     * この SurfaceTexture に描画した内容をエンコードします。
     * </p>
     *
     * @return SurfaceTexture
     */
    protected SurfaceTexture getSurfaceTexture() {
        return mSurfaceDrawingThread != null ? mSurfaceDrawingThread.getSurfaceTexture() : null;
    }

    /**
     * Surface への描画準備が完了したことを通知します.
     */
    protected void onStartSurfaceDrawing() {
    }

    /**
     * Surface への描画が終了したことを通知します.
     */
    protected void onStopSurfaceDrawing() {
    }

    /**
     * Surface への描画を行ったことを通知します.
     */
    protected void onDrawnSurface() {
    }
}
