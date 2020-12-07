package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.gles.WindowSurface;
import org.deviceconnect.android.libmedia.streaming.gles.EGLCore;
import org.deviceconnect.android.libmedia.streaming.gles.SurfaceTextureManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Camera2 API から Surface でプレビューを取得して、エンコードするためのエンコーダ.
 */
public abstract class SurfaceVideoEncoder extends VideoEncoder {

    /**
     * Surface の描画を行うスレッド.
     */
    private EGLSurfaceDrawingThread mSurfaceDrawingThread;

    /**
     * 描画を行う Surface のリスト.
     */
    private List<Surface> mOutputSurfaces = new ArrayList<>();

    /**
     * MediaCodec の入力用 Surface.
     */
    private Surface mMediaCodecSurface;

    /**
     * SurfaceDrawingThread の開始フラグ.
     */
    private boolean mStartSurfaceDrawingFlag;

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();
        mMediaCodecSurface = mMediaCodec.createInputSurface();
        addSurface(mMediaCodecSurface);
    }

    @Override
    protected void startRecording() {
        startDrawingThreadInternal();
    }

    @Override
    protected void stopRecording() {
        if (!mStartSurfaceDrawingFlag) {
            // 開始フラグが false の場合には、明示的に開始されていないので、
            // ここで停止処理を行います。
            stopDrawingThreadInternal();
        }
    }

    @Override
    protected void release() {
        if (mMediaCodecSurface != null) {
            removeSurface(mMediaCodecSurface);
            mMediaCodecSurface = null;
        }
        super.release();
    }

    // VideoEncoder

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    /**
     * 描画先の Surface を追加します.
     *
     * @param surface 追加する Surface
     */
    public void addSurface(Surface surface) {
        mOutputSurfaces.add(surface);

        if (isRunningSurfaceDrawingThread()) {
            EGLSurfaceBase eglSurfaceBase = mSurfaceDrawingThread.createEGLSurfaceBase(surface);
            eglSurfaceBase.setTag(surface);
            mSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);
        }
    }

    /**
     * 描画先の Surface を削除します.
     *
     * @param surface 削除する Surface
     */
    public void removeSurface(Surface surface) {
        mOutputSurfaces.remove(surface);

        if (isRunningSurfaceDrawingThread()) {
            mSurfaceDrawingThread.removeEGLSurfaceBase(surface);
        }
    }

    /**
     * Surface への描画を開始します。
     *
     * エンコーダを開始するよりも先に Surface への描画を開始したい場合に使用します。
     */
    public synchronized void startSurfaceDrawing() {
        mStartSurfaceDrawingFlag = true;
        startDrawingThreadInternal();
    }

    /**
     * Surface への描画スレッドを停止します。
     */
    public synchronized void stopSurfaceDrawing() {
        mStartSurfaceDrawingFlag = false;
        stopDrawingThreadInternal();
    }

    /**
     * Surface への描画スレッドを開始します。
     */
    private void startDrawingThreadInternal() {
        if (isRunningSurfaceDrawingThread()) {
            return;
        }

        VideoQuality quality = getVideoQuality();

        mSurfaceDrawingThread = new EGLSurfaceDrawingThread() {
            @Override
            public void onStarted() {
                for (Surface surface : mOutputSurfaces) {
                    mSurfaceDrawingThread.addEGLSurfaceBase(
                            mSurfaceDrawingThread.createEGLSurfaceBase(surface));
                }
                onStartSurfaceDrawing();
            }

            @Override
            public void onStopped() {
                onStopSurfaceDrawing();
            }

            @Override
            public int getDisplayRotation() {
                return SurfaceVideoEncoder.this.getDisplayRotation();
            }

            @Override
            public void onError(Exception e) {
                postOnError(new MediaEncoderException(e));
            }
        };
        mSurfaceDrawingThread.setName("Surface-Drawing-Thread");
        mSurfaceDrawingThread.setSize(quality.getVideoWidth(), quality.getVideoHeight());
        mSurfaceDrawingThread.start();
    }

    /**
     * Surface への描画スレッドを停止します。
     */
    private void stopDrawingThreadInternal() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.terminate();
            mSurfaceDrawingThread = null;
        }
    }

    /**
     * SurfaceDrawingThread が動作している確認します.
     *
     * @return SurfaceDrawingThread が動作している場合はtrue、それ以外はfalse
     */
    private synchronized boolean isRunningSurfaceDrawingThread() {
        return mSurfaceDrawingThread != null && mSurfaceDrawingThread.isRunning();
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
    protected abstract void onStartSurfaceDrawing();

    /**
     * Surface への描画が終了したことを通知します.
     */
    protected abstract void onStopSurfaceDrawing();
}
