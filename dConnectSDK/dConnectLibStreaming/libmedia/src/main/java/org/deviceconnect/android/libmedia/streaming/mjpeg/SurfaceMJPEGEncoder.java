package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.gles.EGLCore;
import org.deviceconnect.android.libmedia.streaming.gles.OffscreenSurface;
import org.deviceconnect.android.libmedia.streaming.gles.SurfaceTextureManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public abstract class SurfaceMJPEGEncoder extends MJPEGEncoder {
    /**
     * 描画を行う Surface クラス.
     */
    private OffscreenSurface mOffscreenSurface;

    /**
     * SurfaceTextureを管理するクラス.
     */
    private SurfaceTextureManager mStManager;

    /**
     * ピクセル情報を格納するバッファ.
     */
    private Buffer mBuffer;

    /**
     * JPEGに変換するために使用するBitmap.
     */
    private Bitmap mBitmap;

    /**
     * MJPEG のエンコードを行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * JPEG のデータを格納するためのストリーム.
     */
    private final ByteArrayOutputStream mJPEGOutputStream = new ByteArrayOutputStream();

    /**
     * エンコードを開始します.
     */
    public synchronized void start() {
        if (mWorkThread != null) {
            return;
        }

        mWorkThread = new WorkThread();
        mWorkThread.setName("MJPEG-ENCODER");
        mWorkThread.setPriority(Thread.MIN_PRIORITY);
        mWorkThread.start();
    }

    /**
     * エンコードを停止します.
     */
    public synchronized void stop() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }
    }

    /**
     * 解像度の縦横のサイズをスワップするか判断します.
     *
     * @return スワップする場合は true、それ以外は false
     */
    public boolean isSwappedDimensions() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

    /**
     * 画面の回転を取得します.
     *
     * @return 画面の回転
     */
    protected int getDisplayRotation() {
        return Surface.ROTATION_0;
    }

    /**
     * エンコーダーの準備を行います.
     *
     * @throws IOException エンコーダーの準備に失敗した場合に発生
     */
    protected abstract void prepare() throws IOException;

    /**
     * エンコードを開始します.
     *
     * @throws IOException 開始に失敗した場合に発生
     */
    protected abstract void startRecording() throws IOException;

    /**
     * エンコードを停止します.
     */
    protected abstract void stopRecording();

    /**
     * エンコーダーの破棄処理を行います.
     */
    protected abstract void release();

    /**
     * 描画を行う SurfaceTexture を取得します.
     *
     * <p>
     * この SurfaceTexture に描画された内容を JPEG にエンコードします。
     * </p>
     *
     * @return SurfaceTexture
     */
    public SurfaceTexture getSurfaceTexture() {
        return mStManager != null ? mStManager.getSurfaceTexture() : null;
    }

    /**
     * 映像を JPEG に変換します.
     *
     * @param w 映像の横幅
     * @param h 映像の縦幅
     */
    private void drainEncoder(int w, int h) {
        // OpenGLES からピクセルデータを取得するバッファを作成
        if (mBuffer == null || w != mBitmap.getWidth() || h != mBitmap.getHeight()) {
            mBuffer = ByteBuffer.allocateDirect(w * h * 4);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        mBuffer.rewind();

        mOffscreenSurface.readPixelBuffer(mBuffer, w, h);

        mJPEGOutputStream.reset();
        mBitmap.copyPixelsFromBuffer(mBuffer);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, getMJPEGQuality().getQuality(), mJPEGOutputStream);

        postJPEG(mJPEGOutputStream.toByteArray());
    }

    protected OffscreenSurface createOffscreenSurface(EGLCore core) {
        int w = getMJPEGQuality().getWidth();
        int h = getMJPEGQuality().getHeight();
        if (isSwappedDimensions()) {
            w = getMJPEGQuality().getHeight();
            h = getMJPEGQuality().getWidth();
        }
        return new OffscreenSurface(core, w, h);
    }

    /**
     * MJPEG 変換処理を行うスレッド.
     */
    private class WorkThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * OpenGLES のコンテキストなどを管理するクラス.
         */
        private EGLCore mEGLCore;

        /**
         * 停止処理を行います.
         */
        void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * SurfaceTextureManager を作成します.
         */
        private synchronized void createStManager() {
            if (mStManager != null) {
                return;
            }
            mStManager = new SurfaceTextureManager(true);

            MJPEGQuality quality = getMJPEGQuality();
            SurfaceTexture st = mStManager.getSurfaceTexture();
            st.setDefaultBufferSize(quality.getWidth(), quality.getHeight());
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

        @Override
        public void run() {
            try {
                prepare();

                mEGLCore = new EGLCore();

                mOffscreenSurface = createOffscreenSurface(mEGLCore);
                mOffscreenSurface.makeCurrent();

                createStManager();

                startRecording();

                SurfaceTexture st = mStManager.getSurfaceTexture();

                int fps = 1000 / getMJPEGQuality().getFrameRate();
                while (!mStopFlag) {
                    long startTime = System.currentTimeMillis();

                    mStManager.awaitNewImage();
                    mStManager.drawImage(getDisplayRotation());
                    mOffscreenSurface.setPresentationTime(st.getTimestamp());
                    mOffscreenSurface.swapBuffers();

                    drainEncoder(mOffscreenSurface.getWidth(), mOffscreenSurface.getHeight());

                    long drawTime = System.currentTimeMillis() - startTime;
                    if (drawTime < fps) {
                        Thread.sleep(fps - drawTime);
                    }
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                stopRecording();

                release();

                if (mOffscreenSurface != null) {
                    mOffscreenSurface.release();
                    mOffscreenSurface = null;
                }

                releaseStManager();

                // EGLCore を release するタイミングは一番最後にすること。
                // 先に EGLSurfaceBase よりも先に release を行うと、EGLSurfaceBase
                // の release に失敗して、メモリリークになります。
                if (mEGLCore != null) {
                    mEGLCore.release();
                    mEGLCore = null;
                }
            }
        }
    }
}
