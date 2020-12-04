package org.deviceconnect.android.libmedia.streaming.gles;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.Surface;

public class WindowSurface extends EGLSurfaceBase {
    /**
     * 描画先の Surface.
     */
    private Surface mSurface;

    /**
     * Creates a CodecInputSurface from a Surface.
     */
    public WindowSurface(EGLCore core, Surface surface) {
        super(core);
        if (surface == null) {
            throw new IllegalArgumentException("surface is null.");
        }
        mSurface = surface;
        setEGLSurface(createEGLSurface(core.getEGLDisplay(), core.getEGLConfigs(), surface));
    }

    /**
     * 描画先の Surface を取得します.
     */
    public Surface getSurface() {
        return mSurface;
    }

    private EGLSurface createEGLSurface(EGLDisplay display, EGLConfig[] configs, Surface surface) {
        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(display, configs[0], surface, surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
        return eglSurface;
    }
}
