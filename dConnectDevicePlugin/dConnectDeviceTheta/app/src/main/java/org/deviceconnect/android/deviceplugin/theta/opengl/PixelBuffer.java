/*
 PixelBuffer.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.opengl;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEFAULT_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_HEIGHT;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_RENDERABLE_TYPE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_WIDTH;
import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

/**
 * Pixel Buffer.
 *
 * @author NTT DOCOMO, INC.
 */
public class PixelBuffer {

    private static final String TAG = "PixelBuffer";

    private final int mWidth;
    private final int mHeight;
    private final EGL10 mEGL;
    private final EGLDisplay mEGLDisplay;
    private final EGLConfig[] mEGLConfigs;
    private final EGLConfig mEGLConfig;
    private final EGLContext mEGLContext;
    private final EGLSurface mEGLSurface;
    private final GL10 mGL;
    private final String mThreadOwner;

    private GLSurfaceView.Renderer mRenderer;
    private IntBuffer mIb;
    private IntBuffer mIbt;
    private final Bitmap mBitmap;

    public PixelBuffer(final int width, final int height, final boolean isStereo) {
        mWidth = isStereo ? width * 2 : width;
        mHeight = height;
        mIb = IntBuffer.allocate(mWidth * mHeight);
        mIbt = IntBuffer.allocate(mWidth * mHeight);
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mEGL = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        mEGL.eglInitialize(mEGLDisplay, version);
        mEGLConfigs = chooseConfigs();
        mEGLConfig = mEGLConfigs[0];
        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL_NO_CONTEXT, new int[] {
                0x3098, // EGL_CONTEXT_CLIENT_VERSION
                2,      // OpenGL ES 2.0
                EGL10.EGL_NONE });

        mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, new int[] {
                EGL_WIDTH, mWidth,
                EGL_HEIGHT, mHeight,
                EGL_NONE
            });

        mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        mGL = (GL10) mEGLContext.getGL();

        mThreadOwner = Thread.currentThread().getName();
    }

    private void checkEGLError(EGL10 egl) {
        int error = egl.eglGetError();
        if (BuildConfig.DEBUG) {
            if (error == EGL10.EGL_SUCCESS) {
                Log.i(TAG, "EGL SUCCESS.");
            } else {
                Log.e(TAG, "EGL Error: " + error);
            }
        }
    }

    public void destroy() {
        mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        mBitmap.recycle();
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;

        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }

        mRenderer.onSurfaceCreated(mGL, mEGLConfig);
    }

    public void render() {
        if (mRenderer == null) {
            Log.e(TAG, "PixelBuffer.render: Renderer was not set.");
            return;
        }

        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "PixelBuffer.render: This thread does not own the OpenGL context.");
            return;
        }

        mRenderer.onDrawFrame(mGL);
    }

    private EGLConfig[] chooseConfigs() {
        int[] attribList = new int[] {
            EGL_DEPTH_SIZE, 0,
            EGL_STENCIL_SIZE, 0,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_RENDERABLE_TYPE, 4, // OpenGL ES 2.0
            EGL_NONE
        };

        int[] numConfig = new int[1];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, null, 0, numConfig);
        int configSize = numConfig[0];
        EGLConfig[] eGLConfigs = new EGLConfig[configSize];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, eGLConfigs, configSize, numConfig);

        return eGLConfigs;
    }

    public Bitmap convertToBitmap() {
        mGL.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, mIb);
        mBitmap.copyPixelsFromBuffer(mIb);
        mIb.clear();
        mIbt.clear();
        return mBitmap;
    }
}