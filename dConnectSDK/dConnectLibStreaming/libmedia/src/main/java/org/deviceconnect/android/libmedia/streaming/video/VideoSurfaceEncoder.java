package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.gles.CodecInputSurface;
import org.deviceconnect.android.libmedia.streaming.gles.SurfaceTextureManager;

import java.io.IOException;

/**
 * Camera2 API から Surface でプレビューを取得して、エンコードするためのエンコーダ.
 */
public abstract class VideoSurfaceEncoder extends VideoEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VIDEO-SURFACE-ENCODER";

    /**
     * SurfaceTexture管理クラス.
     */
    private SurfaceTextureManager mStManager;

    /**
     * 描画結果を MediaCodec に渡すクラス.
     */
    private CodecInputSurface mInputSurface;

    /**
     * Surface の描画を行うスレッド.
     */
    private SurfaceDrawingThread mSurfaceDrawingThread;

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();
        mInputSurface = new CodecInputSurface(mMediaCodec.createInputSurface());
    }

    @Override
    protected void startRecording() {
        if (mSurfaceDrawingThread != null) {
            if (DEBUG) {
                Log.w(TAG, "SurfaceDrawingThread is already started.");
            }
            return;
        }
        mSurfaceDrawingThread = new SurfaceDrawingThread();
        mSurfaceDrawingThread.setName("Surface-Drawing-Thread");
        mSurfaceDrawingThread.start();
    }

    @Override
    protected void stopRecording() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.terminate();
            mSurfaceDrawingThread = null;
        }
    }

    @Override
    protected void release() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.terminate();
            mSurfaceDrawingThread = null;
        }

        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }

        super.release();
    }

    // VideoEncoder

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
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
         * スレッドを終了します.
         */
        private void terminate() {
            if (mStManager != null) {
                mStManager.release();
                mStManager = null;
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        @Override
        public void run() {
            try {
                mInputSurface.makeCurrent();

                mStManager = new SurfaceTextureManager();

                onStartSurfaceDrawing();

                while (!isInterrupted()) {
                    executeRequest();

                    SurfaceTexture st = mStManager.getSurfaceTexture();

                    mStManager.awaitNewImage();
                    mStManager.drawImage(getDisplayRotation());
                    mInputSurface.setPresentationTime(st.getTimestamp());
                    mInputSurface.swapBuffers();
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                if (mStManager != null) {
                    mStManager.release();
                    mStManager = null;
                }

                onStopSurfaceDrawing();
            }
        }
    }
}
