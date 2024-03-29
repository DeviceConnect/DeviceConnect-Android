package org.deviceconnect.android.libmedia.streaming.gles;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;

public class SurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "ST-MANAGER";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private SurfaceTexture mSurfaceTexture;
    private SurfaceTextureRenderer mTextureRenderer;

    private final Object mFrameSyncObject = new Object();
    private boolean mFrameAvailable;
    private int mTimeoutWaitRefresh = 10000;

    /**
     * Creates instances of SurfaceTextureRenderer and SurfaceTexture.
     */
    public SurfaceTextureManager() {
        this(false);
    }

    /**
     * Creates instances of SurfaceTextureRenderer and SurfaceTexture.
     *
     * @param inverse テクスチャの反転フラグ
     */
    public SurfaceTextureManager(boolean inverse) {
        this(new SurfaceTextureRenderer(inverse));
    }

    /**
     * Creates instances of SurfaceTexture.
     *
     * @param renderer SurfaceTextureRenderer
     */
    public SurfaceTextureManager(SurfaceTextureRenderer renderer) {
        mTextureRenderer = renderer;
        mTextureRenderer.surfaceCreated();

        if (DEBUG) {
            Log.d(TAG, "textureID=" + mTextureRenderer.getTextureId());
        }

        mSurfaceTexture = new SurfaceTexture(mTextureRenderer.getTextureId());

        // This doesn't work if this object is created on the thread that CTS started for
        // these test cases.
        //
        // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
        // create a Handler that uses it.  The "frame available" message is delivered
        // there, but since we're not a Looper-based thread we'll never see it.  For
        // this to do anything useful, OutputSurface must be created on a thread without
        // a Looper, so that SurfaceTexture uses the main application Looper instead.
        //
        // Java language note: passing "this" out of a constructor is generally unwise,
        // but we should be able to get away with it here.
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    public void setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout cannot set negative value.");
        }
        mTimeoutWaitRefresh = timeout;
    }

    /**
     * Release the TextureRender and SurfaceTexture.
     */
    public void release() {
        // this causes a bunch of warnings that appear harmless but might confuse someone:
        //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //mSurfaceTexture.release();

        if (mTextureRenderer != null) {
            mTextureRenderer.surfaceDestroy();
        }
        mTextureRenderer = null;

        if (mSurfaceTexture != null) {
            mSurfaceTexture.setOnFrameAvailableListener(null);
        }
        mSurfaceTexture = null;
    }

    /**
     * Returns the SurfaceTexture.
     */
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the OutputSurface object.
     */
    public void awaitNewImage() {
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(mTimeoutWaitRefresh);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Camera frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    return;
                }
            }
            mFrameAvailable = false;
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
    }

    /**
     * Viewport の設定を行います.
     *
     * @param x x座標
     * @param y y座標
     * @param width 横幅
     * @param height 縦幅
     */
    public void setViewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
    }

    /**
     * 描画する範囲を設定します.
     *
     * @param l 左座標
     * @param t 上座標
     * @param r 右座標
     * @param b 下座標
     * @param width 映像の横幅
     * @param height 映像の縦幅
     */
    public void setCropRect(int l, int t, int r, int b, int width, int height) {
        mTextureRenderer.setCropRect(l, t, r, b, width, height);
    }

    /**
     * 描画する範囲を設定します.
     *
     * @param rect 描画する範囲
     * @param width 映像の横幅
     * @param height 映像の縦幅
     */
    public void setCropRect(Rect rect, int width, int height) {
        mTextureRenderer.setCropRect(rect, width, height);
    }

    /**
     * 描画範囲を解除します.
     */
    public void clearCropRect() {
        mTextureRenderer.clearCropRect();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage(int displayRotation) {
        mTextureRenderer.drawFrame(mSurfaceTexture, displayRotation);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }
}
