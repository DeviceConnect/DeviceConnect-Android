package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
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
     * SurfaceTexture管理クラス.
     */
    private SurfaceTextureManager mStManager;

    /**
     * Surface の描画を行うスレッド.
     */
    private SurfaceDrawingThread mSurfaceDrawingThread;

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
            mSurfaceDrawingThread.addSurface(surface);
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
            mSurfaceDrawingThread.removeSurface(surface);
        }
    }

    /**
     * Surface への描画を開始します。
     */
    public synchronized void startSurfaceDrawing() {
        mStartSurfaceDrawingFlag = true;
        startDrawingThreadInternal();
    }

    /**
     * Surface への描画を停止します。
     */
    public synchronized void stopSurfaceDrawing() {
        mStartSurfaceDrawingFlag = false;
        stopDrawingThreadInternal();
    }

    /**
     * Surface への描画を開始します。
     */
    private void startDrawingThreadInternal() {
        if (isRunningSurfaceDrawingThread()) {
            return;
        }
        mSurfaceDrawingThread = new SurfaceDrawingThread();
        mSurfaceDrawingThread.setName("Surface-Drawing-Thread");
        mSurfaceDrawingThread.start();
    }

    /**
     * Surface への描画を停止します。
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
        return mSurfaceDrawingThread != null && !mSurfaceDrawingThread.mStopFlag;
    }

    /**
     * SurfaceTextureManager を作成します.
     */
    private synchronized void createStManager() {
        if (mStManager != null) {
            return;
        }
        mStManager = new SurfaceTextureManager();

        // SurfaceTexture に解像度を設定
        VideoQuality quality = getVideoQuality();
        SurfaceTexture st = mStManager.getSurfaceTexture();
        st.setDefaultBufferSize(quality.getVideoWidth(), quality.getVideoHeight());
    }

    /**
     * SurfaceTextureManager の後始末を行います.
     */
    private synchronized void releaseStManager() {
        if (mStManager != null) {
            mStManager.release();
            mStManager = null;
        }
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
        return mStManager != null ? mStManager.getSurfaceTexture() : null;
    }

    /**
     * Surface への描画準備が完了したことを通知します.
     */
    protected abstract void onStartSurfaceDrawing();

    /**
     * Surface への描画が終了したことを通知します.
     */
    protected abstract void onStopSurfaceDrawing();

    /**
     * Surface への描画を行うスレッド.
     */
    private class SurfaceDrawingThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * OpenGLES のコンテキストなどを管理するクラス.
         */
        private EGLCore mEGLCore;

        /**
         * 描画先の EGLSurface のリスト.
         */
        private final List<EGLSurfaceBase> mEGLSurfaceBases = new ArrayList<>();

        /**
         * スレッドを終了します.
         */
        private void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        private void addSurface(Surface surface) {
            synchronized (mEGLSurfaceBases) {
                mEGLSurfaceBases.add(new WindowSurface(mEGLCore, surface));
            }
        }

        private void removeSurface(Surface surface) {
            synchronized (mEGLSurfaceBases) {
                EGLSurfaceBase removedSurfaceBase = null;
                for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                    if (eglSurfaceBase instanceof WindowSurface) {
                        if (((WindowSurface) eglSurfaceBase).getSurface() == surface) {
                            removedSurfaceBase = eglSurfaceBase;
                            mEGLSurfaceBases.remove(eglSurfaceBase);
                            break;
                        }
                    }
                }
                if (removedSurfaceBase != null) {
                    removedSurfaceBase.release();
                }
            }
        }

        @Override
        public void run() {
            try {
                mEGLCore = new EGLCore();
                mEGLCore.makeCurrent();

                for (Surface surface : mOutputSurfaces) {
                    mEGLSurfaceBases.add(new WindowSurface(mEGLCore, surface));
                }

                createStManager();

                onStartSurfaceDrawing();

                int fps = 1000 / getVideoQuality().getFrameRate();

                int displayRotation = getDisplayRotation();
                while (!mStopFlag) {
                    long startTime = System.currentTimeMillis();

                    executeRequest();

                    SurfaceTexture st = mStManager.getSurfaceTexture();

                    mStManager.awaitNewImage();

                    synchronized (mEGLSurfaceBases) {
                        for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                            eglSurfaceBase.makeCurrent();
                            mStManager.setViewport(0, 0, eglSurfaceBase.getWidth(), eglSurfaceBase.getHeight());
                            mStManager.drawImage(displayRotation);
                            eglSurfaceBase.setPresentationTime(st.getTimestamp());
                            eglSurfaceBase.swapBuffers();
                        }
                    }

                    long drawTime = System.currentTimeMillis() - startTime;
                    if (drawTime < fps) {
                        Thread.sleep(fps - drawTime);
                    }
                }
            } catch (Exception e) {
                if (!mStopFlag) {
                    postOnError(new MediaEncoderException(e));
                }
            } finally {
                for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                    eglSurfaceBase.release();
                }
                mEGLSurfaceBases.clear();

                // EGLCore を release するタイミングは一番最後にすること。
                // 先に BaseSurface よりも先に release を行うと、BaseSurface
                // の release に失敗して、メモリリークになります。
                if (mEGLCore != null) {
                    mEGLCore.release();
                    mEGLCore = null;
                }

                releaseStManager();

                onStopSurfaceDrawing();
            }
        }
    }
}
