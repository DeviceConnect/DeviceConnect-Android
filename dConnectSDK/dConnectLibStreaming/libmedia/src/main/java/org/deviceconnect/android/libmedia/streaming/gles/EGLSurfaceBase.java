package org.deviceconnect.android.libmedia.streaming.gles;

import android.graphics.Rect;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

import java.nio.Buffer;

/**
 * OpenGLES で描画を行うための EGLSurface をラッピングするクラス.
 */
public abstract class EGLSurfaceBase {
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private EGLCore mEGLCore;
    private int mWidth = -1;
    private int mHeight = -1;
    private Object mTag;
    private Rect mDrawingRange;

    EGLSurfaceBase() {
    }

    EGLSurfaceBase(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width is zero or negative value.");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("height is zero or negative value.");
        }

        mWidth = width;
        mHeight = height;
    }

    /**
     * EGLSurfaceBase を初期化します.
     *
     * @param core EGLSurfaceBase で使用する EGLCore
     */
    abstract void initEGLSurfaceBase(EGLCore core);

    /**
     * EGLCore を設定します.
     *
     * @param core EGLCore
     */
    protected void setEGLCore(EGLCore core) {
        mEGLCore = core;
    }

    /**
     * EGLSurface を設定します.
     *
     * @param surface EGLSurface のインスタンス
     */
    protected void setEGLSurface(EGLSurface surface) {
        mEGLSurface = surface;
    }

    /**
     * Returns the surface's width, in pixels.
     * <p>
     * If this is called on a window surface, and the underlying surface is in the process
     * of changing size, we may not see the new size right away (e.g. in the "surfaceChanged"
     * callback).  The size should match after the next buffer swap.
     */
    public int getWidth() {
        if (mWidth < 0) {
            return mEGLCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH);
        } else {
            return mWidth;
        }
    }

    /**
     * Returns the surface's height, in pixels.
     */
    public int getHeight() {
        if (mHeight < 0) {
            return mEGLCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT);
        } else {
            return mHeight;
        }
    }

    /**
     * 描画範囲を設定します.
     *
     * @param rect 描画範囲
     */
    public void setDrawingRange(Rect rect) {
        mDrawingRange = rect;
    }

    /**
     * 描画範囲を取得します.
     *
     * 未設定の場合には null を返却します。
     *
     * @return 描画範囲
     */
    public Rect getDrawingRange() {
        return mDrawingRange;
    }

    /**
     * Discards all resources held by this class, notably the EGL context.  Also releases the
     * Surface that was passed to our constructor.
     */
    public void release() {
        if (mEGLCore != null && mEGLCore.getEGLDisplay() != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEGLCore.getEGLDisplay(), mEGLSurface);
        }
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mEGLCore = null;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        EGL14.eglMakeCurrent(mEGLCore.getEGLDisplay(), mEGLSurface, mEGLSurface, mEGLCore.getEGLContext());
        checkEglError("eglMakeCurrent");
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    public boolean swapBuffers() {
        boolean result = EGL14.eglSwapBuffers(mEGLCore.getEGLDisplay(), mEGLSurface);
        checkEglError("eglSwapBuffers");
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        // nsecs に 0 が指定された時に描画が行われない端末があるので、
        // ここでは、一旦コメントアウトしておきます。
//        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
//        checkEglError("eglPresentationTimeANDROID");
    }

    /**
     * 指定したバッファにピクセルデータを格納します.
     * @param buffer ピクセルデータを格納するバッファ
     * @param w 横幅
     * @param h 縦幅
     */
    public void readPixelBuffer(Buffer buffer, int w, int h) {
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
    }

    /**
     * Checks for EGL errors.  Throws an exception if one is found.
     */
    void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    /**
     * タグを設定します.
     *
     * @param tag タグ
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * タグを取得します.
     *
     * @return タグ
     */
    public Object getTag() {
        return mTag;
    }
}
