package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class SurfaceMJPEGEncoder extends MJPEGEncoder {
    /**
     * EGLSurfaceBase を識別するためのタグ.
     */
    private static final Integer TAG_SURFACE = 12345;

    /**
     * ピクセル情報を格納するバッファ.
     */
    private ByteBuffer mBuffer;

    /**
     * JPEGに変換するために使用するBitmap.
     */
    private Bitmap mBitmap;

    /**
     * JPEG のデータを格納するためのストリーム.
     */
    private final ByteArrayOutputStream mJPEGOutputStream = new ByteArrayOutputStream();

    /**
     * Surface の描画を行うスレッド.
     */
    private EGLSurfaceDrawingThread mSurfaceDrawingThread;

    /**
     * 描画を行う Surface のリスト.
     */
    private List<Surface> mOutputSurfaces = new ArrayList<>();

    /**
     * バッファを反転させるために一時的に値を格納するバッファ.
     */
    private byte[] mTmp1;

    /**
     * バッファを反転させるために一時的に値を格納するバッファ.
     */
    private byte[] mTmp2;

    /**
     * SurfaceDrawingThread の開始フラグ.
     */
    private boolean mStartSurfaceDrawingFlag;

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
     * Surface への描画を停止します。
     */
    public synchronized void stopSurfaceDrawing() {
        mStartSurfaceDrawingFlag = false;
        stopDrawingThreadInternal();
    }

    /**
     * エンコードを開始します.
     */
    public synchronized void start() {
        startDrawingThreadInternal();
    }

    /**
     * エンコードを停止します.
     */
    public synchronized void stop() {
        if (!mStartSurfaceDrawingFlag) {
            stopDrawingThreadInternal();
        }
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
        return mSurfaceDrawingThread != null ? mSurfaceDrawingThread.getSurfaceTexture() : null;
    }

    /**
     * SurfaceDrawingThread が動作している確認します.
     *
     * @return SurfaceDrawingThread が動作している場合はtrue、それ以外はfalse
     */
    private synchronized boolean isRunningSurfaceDrawingThread() {
        return mSurfaceDrawingThread != null && mSurfaceDrawingThread.isRunning();
    }

    private void startDrawingThreadInternal() {
        if (isRunningSurfaceDrawingThread()) {
            return;
        }

        MJPEGQuality quality = getMJPEGQuality();
        int w = isSwappedDimensions() ? quality.getHeight() : quality.getWidth();
        int h = isSwappedDimensions() ? quality.getWidth() : quality.getHeight();

        mSurfaceDrawingThread = new EGLSurfaceDrawingThread() {
            @Override
            public void onStarted() {
                EGLSurfaceBase eglSurfaceBase = mSurfaceDrawingThread.createEGLSurfaceBase(w, h);
                eglSurfaceBase.setTag(TAG_SURFACE);
                mSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);

                for (Surface surface : mOutputSurfaces) {
                    mSurfaceDrawingThread.addEGLSurfaceBase(
                            mSurfaceDrawingThread.createEGLSurfaceBase(surface));
                }

                try {
                    prepare();
                    startRecording();
                } catch (IOException e) {
                    postOnError(new MJPEGEncoderException(e));
                }
            }

            @Override
            public void onStopped() {
                stopRecording();
                release();

                mBuffer = null;
                mTmp1 = null;
                mTmp2 = null;
                System.gc();
            }

            @Override
            public int getDisplayRotation() {
                return SurfaceMJPEGEncoder.this.getDisplayRotation();
            }

            @Override
            public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
                if (TAG_SURFACE.equals(eglSurfaceBase.getTag())) {
                    try {
                        drainEncoder(eglSurfaceBase, eglSurfaceBase.getWidth(), eglSurfaceBase.getHeight());
                    } catch (Throwable t) {
                        // ignore.
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                postOnError(new MJPEGEncoderException(e));
            }
        };
        mSurfaceDrawingThread.setName("MJPEG-ENCODER");
        mSurfaceDrawingThread.setSize(w, h);
        mSurfaceDrawingThread.start();
    }

    private void stopDrawingThreadInternal() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.terminate();
            mSurfaceDrawingThread = null;
        }
    }

    /**
     * 映像を JPEG に変換します.
     *
     * @param width 映像の横幅
     * @param height 映像の縦幅
     */
    private void drainEncoder(EGLSurfaceBase surface, int width, int height) {
        // OpenGLES からピクセルデータを取得するバッファを作成
        if (mBuffer == null || width != mBitmap.getWidth() || height != mBitmap.getHeight()) {
            mBuffer = ByteBuffer.allocateDirect(width * height * 4);
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mTmp1 = new byte[width * 4];
            mTmp2 = new byte[width * 4];
        }
        mBuffer.rewind();

        surface.readPixelBuffer(mBuffer, width, height);

        // 上下逆なので、上下反転
        int h = height / 2;
        for (int y = 0; y < h; y++) {
            mBuffer.position(y * width * 4);
            mBuffer.get(mTmp1, 0, mTmp1.length);
            mBuffer.position((height - 1 - y) * width * 4);
            mBuffer.get(mTmp2, 0, mTmp2.length);
            mBuffer.position((height - 1 - y) * width * 4);
            mBuffer.put(mTmp1);
            mBuffer.position(y * width * 4);
            mBuffer.put(mTmp2);
        }
        mBuffer.position(0);

        mJPEGOutputStream.reset();
        mBitmap.copyPixelsFromBuffer(mBuffer);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, getMJPEGQuality().getQuality(), mJPEGOutputStream);

        postJPEG(mJPEGOutputStream.toByteArray());
    }
}
