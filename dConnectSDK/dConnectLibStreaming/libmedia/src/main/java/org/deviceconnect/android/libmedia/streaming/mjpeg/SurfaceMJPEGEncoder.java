package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.graphics.Bitmap;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
     * このフラグが true の場合には、外部から EGLSurfaceDrawingThread が設定されたことを意味します。
     */
    private final boolean mInternalCreateSurfaceDrawingThread;

    /**
     * Surface の描画を行うスレッド.
     */
    private EGLSurfaceDrawingThread mSurfaceDrawingThread;

    /**
     * バッファを反転させるために一時的に値を格納するバッファ.
     */
    private byte[] mTmp1;

    /**
     * バッファを反転させるために一時的に値を格納するバッファ.
     */
    private byte[] mTmp2;

    /**
     * EGLSurfaceDrawingThread のイベントを受け取るリスナー.
     */
    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            MJPEGQuality quality = getMJPEGQuality();
            int w = isSwappedDimensions() ? quality.getHeight() : quality.getWidth();
            int h = isSwappedDimensions() ? quality.getWidth() : quality.getHeight();

            EGLSurfaceBase eglSurfaceBase = mSurfaceDrawingThread.createEGLSurfaceBase(w, h);
            eglSurfaceBase.setTag(TAG_SURFACE);
            mSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);

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
        }

        @Override
        public void onError(Exception e) {
            postOnError(new MJPEGEncoderException(e));
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
    };

    public SurfaceMJPEGEncoder() {
        mInternalCreateSurfaceDrawingThread = true;
    }

    public SurfaceMJPEGEncoder(EGLSurfaceDrawingThread thread) {
        mSurfaceDrawingThread = thread;
        mInternalCreateSurfaceDrawingThread = false;
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
        stopDrawingThreadInternal();
    }

    /**
     * 解像度の縦横のサイズをスワップするか判断します.
     *
     * @return スワップする場合は true、それ以外は false
     */
    public boolean isSwappedDimensions() {
        if (mSurfaceDrawingThread == null) {
            switch (getDisplayRotation()) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    return false;
                default:
                    return true;
            }
        } else {
            return mSurfaceDrawingThread.isSwappedDimensions();
        }
    }

    /**
     * 画面の回転を取得します.
     *
     * @return 画面の回転
     */
    protected int getDisplayRotation() {
        if (mSurfaceDrawingThread == null) {
            return Surface.ROTATION_0;
        } else {
            return mSurfaceDrawingThread.getDisplayRotation();
        }
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
     * SurfaceDrawingThread が動作している確認します.
     *
     * @return SurfaceDrawingThread が動作している場合はtrue、それ以外はfalse
     */
    private synchronized boolean isRunningSurfaceDrawingThread() {
        return mSurfaceDrawingThread != null && mSurfaceDrawingThread.isRunning();
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
     * Surface への描画スレッドを開始します。
     */
    private void startDrawingThreadInternal() {
        MJPEGQuality quality = getMJPEGQuality();
        int w = isSwappedDimensions() ? quality.getHeight() : quality.getWidth();
        int h = isSwappedDimensions() ? quality.getWidth() : quality.getHeight();

        if (mInternalCreateSurfaceDrawingThread) {
            mSurfaceDrawingThread = createEGLSurfaceDrawingThread();
            mSurfaceDrawingThread.setSize(w, h);
            mSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
            mSurfaceDrawingThread.start();
        } else {
            mSurfaceDrawingThread.setSize(w, h);
            mSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
            if (isRunningSurfaceDrawingThread()) {
                EGLSurfaceBase eglSurfaceBase = mSurfaceDrawingThread.createEGLSurfaceBase(w, h);
                eglSurfaceBase.setTag(TAG_SURFACE);
                mSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);
            } else {
                mSurfaceDrawingThread.start();
            }
        }
    }

    /**
     * Surface への描画スレッドを停止します。
     */
    private void stopDrawingThreadInternal() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.removeEGLSurfaceBase(TAG_SURFACE);
            mSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);

            // 内部で作成された場合には、停止処理も行います。
            if (mInternalCreateSurfaceDrawingThread) {
                mSurfaceDrawingThread.terminate();
                mSurfaceDrawingThread = null;
            }
        }

        mBuffer = null;
        mTmp1 = null;
        mTmp2 = null;
        System.gc();
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
