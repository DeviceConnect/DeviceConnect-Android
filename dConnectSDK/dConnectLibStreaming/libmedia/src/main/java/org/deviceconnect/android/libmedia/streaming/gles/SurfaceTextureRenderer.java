package org.deviceconnect.android.libmedia.streaming.gles;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SurfaceTextureRenderer {
    private static final String TAG = "SurfaceTextureRender";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final float[] TRIANGLE_VERTICES_DATA = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.f, 0.f, 0.f,
             1.0f, -1.0f, 0.f, 1.f, 0.f,
            -1.0f,  1.0f, 0.f, 0.f, 1.f,
             1.0f,  1.0f, 0.f, 1.f, 1.f,
    };

    private static final float[] TRIANGLE_VERTICES_DATA_2 = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.f, 0.f, 1.f,
             1.0f, -1.0f, 0.f, 1.f, 1.f,
            -1.0f,  1.0f, 0.f, 0.f, 0.f,
             1.0f,  1.0f, 0.f, 1.f, 0.f,
    };

    private final FloatBuffer mTriangleVertices;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private final float[] mMVPMatrix = new float[16];
    private final float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mVertexShader;
    private int mFragmentShader;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    /**
     * コンストラクタ.
     * @param inverse テクスチャの反転フラグ
     */
    SurfaceTextureRenderer(boolean inverse) {
        mTriangleVertices = ByteBuffer.allocateDirect(
                TRIANGLE_VERTICES_DATA.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        if (inverse) {
            mTriangleVertices.put(TRIANGLE_VERTICES_DATA_2).position(0);
        } else {
            mTriangleVertices.put(TRIANGLE_VERTICES_DATA).position(0);
        }

        Matrix.setIdentityM(mSTMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * テクスチャの ID を取得します.
     *
     * {@link #surfaceCreated()} が呼び出されていない場合には 0 が返却されます。
     *
     * @return テクスチャの ID
     */
    public int getTextureId() {
        return mTextureID;
    }

    /**
     * 描画用の SurfaceTexture を作成します.
     */
    public void surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(maPositionHandle, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(maTextureHandle, "aTextureCoord");

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(muMVPMatrixHandle, "uMVPMatrix");
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkLocation(muSTMatrixHandle, "uSTMatrix");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
    }

    /**
     * 描画用の SurfaceTexture を破棄します.
     */
    public void surfaceDestroy() {
        if (mTextureID != 0) {
            GLES20.glDeleteTextures(1, new int[] {mTextureID}, 0);
            mTextureID = 0;
        }

        if (mVertexShader != 0) {
            GLES20.glDeleteShader(mVertexShader);
            mVertexShader = 0;
        }

        if (mFragmentShader != 0) {
            GLES20.glDeleteShader(mFragmentShader);
            mFragmentShader = 0;
        }

        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    /**
     * 描画処理を行います.
     *
     * @param st テクスチャに使用する SurfaceTexture
     * @param displayRotation 画面の回転
     */
    public void drawFrame(SurfaceTexture st, int displayRotation) {
        st.getTransformMatrix(mSTMatrix);

        switch (displayRotation) {
            default:
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                Matrix.rotateM(mSTMatrix, 0, 270, 0, 0, 1);
                Matrix.translateM(mSTMatrix, 0, -1, 0, 0);
                break;
            case Surface.ROTATION_180:
                Matrix.rotateM(mSTMatrix, 0, 180, 0, 0, 1);
                Matrix.translateM(mSTMatrix, 0, -1, -1, 0);
                break;
            case Surface.ROTATION_270:
                Matrix.rotateM(mSTMatrix, 0, 90, 0, 0, 1);
                Matrix.translateM(mSTMatrix, 0, 0, -1, 0);
                break;
        }

        // (optional) clear to green so we can see if we're failing to set pixels
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(maTextureHandle);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // IMPORTANT: on some devices, if you are sharing the external texture between two
        // contexts, one context may not see updates to the texture unless you un-bind and
        // re-bind it.  If you're not using shared EGL contexts, you don't need to bind
        // texture 0 here.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    /**
     * 描画範囲を設定します.
     *
     * @param left 描画を開始するx座標
     * @param top 描画を開始するy座標
     * @param right 描画を終了するx座標
     * @param bottom 描画を終了するy座標
     * @param width 画像の横幅
     * @param height 画像の縦幅
     */
    public void setDrawingRange(int left, int top, int right, int bottom, int width, int height) {
        float l = left / (float) width;
        float t = 1.0f - bottom / (float) height;
        float r = right / (float) width;
        float b = 1.0f - top / (float) height;
        setDrawingRange(l, t, r, b);
    }

    private void setDrawingRange(float l, float t, float r, float b) {
        float[] triangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0.f, l, t,
                 1.0f, -1.0f, 0.f, r, t,
                -1.0f,  1.0f, 0.f, l, b,
                 1.0f,  1.0f, 0.f, r, b,
        };

        mTriangleVertices.clear();
        mTriangleVertices.put(triangleVerticesData).position(0);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            if (DEBUG) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            }
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (mVertexShader == 0) {
            return 0;
        }

        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (mFragmentShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            if (DEBUG) {
                Log.e(TAG, "Could not create program.");
            }
            return 0;
        }
        GLES20.glAttachShader(program, mVertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, mFragmentShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            if (DEBUG) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            }
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            if (DEBUG) {
                Log.e(TAG, op + ": glError " + error);
            }
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }
}
