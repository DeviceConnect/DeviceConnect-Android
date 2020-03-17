package org.deviceconnect.android.libmedia.streaming.gles;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.Surface;

public class CodecInputSurface extends BaseSurface {
    /**
     * 描画を行う Surface.
     */
    private Surface mSurface;

    /**
     * Creates a CodecInputSurface from a Surface.
     */
    public CodecInputSurface(Surface surface) {
        if (surface == null) {
            throw new NullPointerException();
        }
        mSurface = surface;
        setupEGL();
    }

    @Override
    public void release() {
        super.release();

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    @Override
    EGLSurface createEGLSurface(EGLDisplay display, EGLConfig[] configs) {
        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };

        EGLSurface surface = EGL14.eglCreateWindowSurface(display, configs[0], mSurface, surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
        return surface;
    }
}
