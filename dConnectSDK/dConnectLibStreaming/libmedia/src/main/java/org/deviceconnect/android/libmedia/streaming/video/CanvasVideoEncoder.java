package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;

public abstract class CanvasVideoEncoder extends SurfaceVideoEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CANVAS";

    /**
     * Canvas 描画用のスレッド.
     */
    private DrawThread mThread;

    /**
     * 映像のエンコード設定.
     */
    private VideoQuality mVideoQuality;

    /**
     * コンストラクタ.
     * <p>
     * デフォルトの場合はマイムタイプに video/avc を設定します。
     * </p>
     */
    public CanvasVideoEncoder() {
        this("video/avc");
    }

    /**
     * コンストラクタ.
     * @param mimeType MediaCodec に渡すマイムタイプ
     */
    public CanvasVideoEncoder(String mimeType) {
        this(new VideoQuality(mimeType));
    }

    /**
     * コンストラクタ.
     * @param videoQuality 映像エンコードの設定
     */
    public CanvasVideoEncoder(VideoQuality videoQuality) {
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
        if (mThread != null) {
            mThread.terminate();
        }
        mThread = new DrawThread();
        mThread.setName("CANVAS-DRAW");
        mThread.start();
    }

    @Override
    protected void onStopSurfaceDrawing() {
        if (mThread != null) {
            mThread.terminate();
            mThread = null;
        }
    }

    /**
     * エンコードするための画面を描画します.
     *
     * @param canvas 描画先のキャンバス
     * @param width 描画先の横幅
     * @param height 描画先の縦幅
     */
    public abstract void draw(Canvas canvas, int width, int height);

    /**
     * Canvas に描画を行うためのスレッド.
     */
    private class DrawThread extends Thread {
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
            VideoQuality videoQuality = getVideoQuality();

            int width = videoQuality.getVideoWidth();
            int height = videoQuality.getVideoHeight();
            int fps = 1000 / videoQuality.getFrameRate();

            SurfaceTexture surfaceTexture = getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(videoQuality.getVideoWidth(), videoQuality.getVideoHeight());

            Surface surface = new Surface(getSurfaceTexture());
            try {
                while (!mStopFlag) {
                    long start = System.currentTimeMillis();

                    Canvas canvas = surface.lockCanvas(null);
                    if (canvas == null) {
                        if (DEBUG) {
                            Log.e(TAG, "Failed to lock a canvas.");
                        }
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
                surface.release();
            }
        }
    }
}
