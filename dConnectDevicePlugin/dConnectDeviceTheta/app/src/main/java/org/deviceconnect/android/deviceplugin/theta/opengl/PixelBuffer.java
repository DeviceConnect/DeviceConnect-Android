package org.deviceconnect.android.deviceplugin.theta.opengl;

import static javax.microedition.khronos.egl.EGL10.*;
import static javax.microedition.khronos.opengles.GL10.*;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;

public class PixelBuffer {
    private static final String TAG = "PixelBuffer";
    private static final boolean LIST_CONFIGS = true;

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

    private GLSurfaceView.Renderer mRenderer; // borrow this interface
    private IntBuffer mIb;
    private IntBuffer mIbt;
    private final Bitmap mBitmap;

    public PixelBuffer(final int width, final int height) {
        mWidth = width;
        mHeight = height;
        mIb = IntBuffer.allocate(mWidth * mHeight);
        mIbt = IntBuffer.allocate(mWidth * mHeight);
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        // No error checking performed, minimum required code to elucidate logic
        mEGL = (EGL10) EGLContext.getEGL();
//        checkEGLError(mEGL);
        mEGLDisplay = mEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);
//        checkEGLError(mEGL);
        int[] version = new int[2];
        mEGL.eglInitialize(mEGLDisplay, version);
//        checkEGLError(mEGL);
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "EGL Version: " + version[0] + "." + version[1]);
//        }
//        checkEGLError(mEGL);
        mEGLConfigs = chooseConfigs(); // Choosing a config is a little more complicated
        mEGLConfig = mEGLConfigs[0]; // Best match is probably the first configuration
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "EGL Config: " + mEGLConfig);
//        }
//        checkEGLError(mEGL);
        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL_NO_CONTEXT, new int[] {
                0x3098, // EGL_CONTEXT_CLIENT_VERSION
                2,      // OpenGL ES 2.0
                EGL10.EGL_NONE });
//        checkEGLError(mEGL);
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "EGL Context: " + mEGLContext);
//        }

        mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, new int[] {
                EGL_WIDTH, mWidth,
                EGL_HEIGHT, mHeight,
                EGL_NONE
            });
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "EGL Surface: " + mEGLSurface);
        }

        mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        mGL = (GL10) mEGLContext.getGL();

        // Record thread owner of OpenGL context
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

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }

        // Call the renderer initialization routines
        mRenderer.onSurfaceCreated(mGL, mEGLConfig);
        //mRenderer.onSurfaceChanged(mGL, mWidth, mHeight);
    }

    public Bitmap render() {
        // Do we have a renderer?
        if (mRenderer == null) {
            Log.e(TAG, "PixelBuffer.render: Renderer was not set.");
            return null;
        }

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "PixelBuffer.render: This thread does not own the OpenGL context.");
            return null;
        }

        // Call the renderer draw routine
        mRenderer.onDrawFrame(mGL);
        convertToBitmap();
        return mBitmap;
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

        // No error checking performed, minimum required code to elucidate logic
        // Expand on this logic to be more selective in choosing a configuration
        int[] numConfig = new int[1];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, null, 0, numConfig);
        int configSize = numConfig[0];
        EGLConfig[] eGLConfigs = new EGLConfig[configSize];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, eGLConfigs, configSize, numConfig);

        if (LIST_CONFIGS) {
            listConfig(eGLConfigs);
        }

        return eGLConfigs;
    }

    private void listConfig(EGLConfig[] eGLConfigs) {
        Log.i(TAG, "Config List {");
 
        for (EGLConfig config : eGLConfigs) {
            int d, s, r, g, b, a;
                   
            // Expand on this logic to dump other attributes
            d = getConfigAttrib(config, EGL_DEPTH_SIZE);
            s = getConfigAttrib(config, EGL_STENCIL_SIZE);
            r = getConfigAttrib(config, EGL_RED_SIZE);
            g = getConfigAttrib(config, EGL_GREEN_SIZE);
            b = getConfigAttrib(config, EGL_BLUE_SIZE);
            a = getConfigAttrib(config, EGL_ALPHA_SIZE);
            Log.i(TAG, "    <d,s,r,g,b,a> = <" + d + "," + s + "," +
                r + "," + g + "," + b + "," + a + ">");
        }
 
        Log.i(TAG, "}");
    }

    private int getConfigAttrib(EGLConfig config, int attribute) {
        int[] value = new int[1];
        return mEGL.eglGetConfigAttrib(mEGLDisplay, config,
                        attribute, value)? value[0] : 0;
    }

    private void convertToBitmap() {
        long start = System.currentTimeMillis();

        mGL.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, mIb);

        Log.i("AAA", "Finished to glReadPixels: " + (System.currentTimeMillis() - start) + " msec");
        start = System.currentTimeMillis();

//        // Convert upside down mirror-reversed image to right-side up normal image.
//        for (int i = 0; i < mHeight; i++) {
//            for (int j = 0; j < mWidth; j++) {
//                mIbt.put((mHeight - i - 1) * mWidth + j, mIb.get(i * mWidth + j));
//            }
//        }
//
//        Log.i("AAA", "Finished to convert bitmap: " + (System.currentTimeMillis() - start) + " msec");
//        start = System.currentTimeMillis();
//        mBitmap.copyPixelsFromBuffer(mIbt);

        mBitmap.copyPixelsFromBuffer(mIb);

        mIb.clear();
        mIbt.clear();
    }
}