package org.deviceconnect.android.libmedia.streaming.gles;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

/**
 * オフスクリーン用の Surface.
 */
public class OffscreenSurface extends BaseSurface {
    /**
     * オフスクリーンの横幅.
     */
    private int mWidth;

    /**
     * オフスクリーンの縦幅.
     */
    private int mHeight;

    /**
     * 縦幅、横幅から OffscreenSurface を作成します.
     *
     * @param width オフスクリーンの横幅
     * @param height オフスクリーンの縦幅
     */
    public OffscreenSurface(int width, int height) {
        mWidth = width;
        mHeight = height;
        setupEGL();
    }

    /**
     * オフスクリーンの横幅を取得します.
     *
     * @return オフスクリーンの横幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * オフスクリーンの縦幅を取得します.
     *
     * @return オフスクリーンの縦幅
     */
    public int getHeight() {
        return mHeight;
    }

    @Override
    EGLSurface createEGLSurface(EGLDisplay display, EGLConfig[] configs) {
        // Create a window surface, and attach it to the Surface we received.
        final int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, mWidth,
                EGL14.EGL_HEIGHT, mHeight,
                EGL14.EGL_NONE
        };

        EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0);
        checkEglError("eglCreatePbufferSurface");
        return surface;
    }
}
