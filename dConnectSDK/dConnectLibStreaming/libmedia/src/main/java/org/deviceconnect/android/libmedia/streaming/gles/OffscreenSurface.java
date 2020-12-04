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
     * 縦幅、横幅から OffscreenSurface を作成します.
     *
     * @param width オフスクリーンの横幅
     * @param height オフスクリーンの縦幅
     */
    public OffscreenSurface(EGLCore core, int width, int height) {
        super(core, width, height);
        if (width <= 0) {
            throw new IllegalArgumentException("width is zero or negative value.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height is zero or negative value.");
        }
        setEGLSurface(createEGLSurface(core.getEGLDisplay(), core.getEGLConfigs(), width, height));
    }

    private EGLSurface createEGLSurface(EGLDisplay display, EGLConfig[] configs, int width, int height) {
        // Create a window surface, and attach it to the Surface we received.
        final int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0);
        checkEglError("eglCreatePbufferSurface");
        return eglSurface;
    }
}
