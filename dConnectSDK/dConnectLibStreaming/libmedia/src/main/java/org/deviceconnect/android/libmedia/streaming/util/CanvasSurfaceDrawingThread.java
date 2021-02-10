package org.deviceconnect.android.libmedia.streaming.util;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class CanvasSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * Canvas に描画する FPS.
     */
    private int mFps = 30;

    /**
     * Canvas を描画するスレッド.
     */
    private CanvasDrawingThread mDrawingThread;

    /**
     * Canvas への描画する FPS を設定します.
     *
     * デフォルトでは、30 に設定してあります。
     *
     * @param fps 描画を行う FPS
     */
    public void setFps(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps is zero or negative value.");
        }
        mFps = fps;
    }

    @Override
    protected void onStarted() {
        if (mDrawingThread != null) {
            mDrawingThread.terminate();
        }
        mDrawingThread = new CanvasDrawingThread();
        mDrawingThread.setName("Canvas-Drawing");
        mDrawingThread.start();
    }

    @Override
    protected void onStopped() {
        if (mDrawingThread != null) {
            mDrawingThread.terminate();
            mDrawingThread = null;
        }
    }

    /**
     * Canvas への描画を行います.
     *
     * @param canvas 描画を行う Canvas
     * @param width Canvas の横幅
     * @param height Canvas の縦幅
     */
    public void draw(Canvas canvas, int width, int height) {
    }

    private class CanvasDrawingThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * 描画スレッドを停止します.
         */
        private void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            Surface surface = null;
            try {
                int width = getWidth();
                int height = getHeight();
                int fps = 1000 / mFps;

                SurfaceTexture surfaceTexture = getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(width, height);

                surface = new Surface(surfaceTexture);

                while (!mStopFlag) {
                    long start = System.currentTimeMillis();

                    Canvas canvas = surface.lockCanvas(null);
                    if (canvas == null) {
                        break;
                    }

                    try {
                        draw(canvas, width, height);
                    } finally {
                        surface.unlockCanvasAndPost(canvas);
                    }

                    long t = fps - (System.currentTimeMillis() - start);
                    if (t > 0) {
                        Thread.sleep(t);
                    }
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (!mStopFlag) {
                    postOnError(new MediaEncoderException(e));
                }
            } finally {
                if (surface != null) {
                    surface.release();
                }
            }
        }
    }
}
