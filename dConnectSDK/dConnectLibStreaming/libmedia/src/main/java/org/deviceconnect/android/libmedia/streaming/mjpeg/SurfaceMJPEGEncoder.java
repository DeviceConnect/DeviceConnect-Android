package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.io.ByteArrayOutputStream;
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
     * true の場合は JPEG に変換中。
     */
    private boolean mEncodeFlag;

    /**
     * JPEG にエンコードするためのスレッド.
     */
    private EncoderThread mEncoderThread;

    /**
     * JPEG の送信時間を格納する.
     */
    private long mSendTime;

    /**
     * EGLSurfaceDrawingThread のイベントを受け取るリスナー.
     */
    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            try {
                prepare();
                startRecording();
            } catch (Exception e) {
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

    @Override
    public void start() {
        startDrawingThreadInternal();
    }

    @Override
    public void stop() {
        stopDrawingThreadInternal();
    }

    /**
     * エンコーダーの準備を行います.
     */
    protected void prepare() {
    }

    /**
     * エンコードを開始します.
     */
    protected void startRecording() {
    }

    /**
     * エンコードを停止します.
     */
    protected void stopRecording() {
    }

    /**
     * エンコーダーの破棄処理を行います.
     */
    protected void release() {
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
    private synchronized void startDrawingThreadInternal() {
        MJPEGQuality quality = getMJPEGQuality();

        if (mEncoderThread != null) {
            mEncoderThread.terminate();
        }
        mEncoderThread = new EncoderThread();
        mEncoderThread.setName("JPEG-ENCODE");
        mEncoderThread.start();
        mEncodeFlag = false;

        if (mInternalCreateSurfaceDrawingThread) {
            mSurfaceDrawingThread = createEGLSurfaceDrawingThread();
        }

        int w = quality.getWidth();
        int h = quality.getHeight();
        Rect rect = quality.getDrawingRange();
        if (rect != null) {
            w = rect.width();
            h = rect.height();
        }
        mSurfaceDrawingThread.setSize(quality.getWidth(), quality.getHeight());
        mSurfaceDrawingThread.addEGLSurfaceBase(w, h, TAG_SURFACE, quality.getDrawingRange());
        mSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
        mSurfaceDrawingThread.start();
    }

    /**
     * Surface への描画スレッドを停止します。
     */
    private synchronized void stopDrawingThreadInternal() {
        if (mSurfaceDrawingThread != null) {
            mSurfaceDrawingThread.removeEGLSurfaceBase(TAG_SURFACE);
            mSurfaceDrawingThread.stop(false);
            mSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            if (mInternalCreateSurfaceDrawingThread) {
                mSurfaceDrawingThread = null;
            }
        }

        if (mEncoderThread != null) {
            mEncoderThread.terminate();
            mEncoderThread = null;
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
        long nowTime = System.currentTimeMillis();
        if (nowTime - mSendTime < getMJPEGQuality().getFrameRateMSEC()) {
            return;
        }

        if (mEncodeFlag || mEncoderThread == null) {
            return;
        }
        mEncodeFlag = true;
        mSendTime = nowTime;

        // OpenGLES からピクセルデータを取得するバッファを作成
        if (mBuffer == null || width != mBitmap.getWidth() || height != mBitmap.getHeight()) {
            mBuffer = ByteBuffer.allocateDirect(width * height * 4);
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mTmp1 = new byte[width * 4];
            mTmp2 = new byte[width * 4];
        }
        mBuffer.rewind();

        surface.readPixelBuffer(mBuffer, width, height);

        // JPEG へのエンコードは処理が重いので、別スレッドで行うようにしています。
        mEncoderThread.add(() -> {
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

            mEncodeFlag = false;
        });
    }

    /**
     * JPEG にエンコードするためのスレッド.
     */
    private static class EncoderThread extends QueueThread<Runnable> {
        private void terminate() {
            interrupt();

            try {
                join(300);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    get().run();
                }
            } catch (Throwable e) {
                // ignore.
            }
        }
    }
}
