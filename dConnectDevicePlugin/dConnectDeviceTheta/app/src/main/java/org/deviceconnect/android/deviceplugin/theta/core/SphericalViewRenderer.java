package org.deviceconnect.android.deviceplugin.theta.core;


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import org.deviceconnect.android.deviceplugin.theta.opengl.model.UVSphere;
import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SphericalViewRenderer implements GLSurfaceView.Renderer {

    /**
     * Logger.
     */
    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    /**
     * Distance of left and right eye: {@value} cm.
     */
    private static final float DISTANCE_EYES = 10.0f / 100.0f;
    /**
     * Radius of sphere for photo.
     */
    private static final float DEFAULT_TEXTURE_SHELL_RADIUS = 1.0f;
    /**
     * Number of sphere polygon partitions for photo, which must be an even number.
     */
    private static final int SHELL_DIVIDES = 40;

    private static final String VSHADER_SRC =
        "attribute vec4 aPosition;\n" +
            "attribute vec2 aUV;\n" +
            "uniform mat4 uProjection;\n" +
            "uniform mat4 uView;\n" +
            "uniform mat4 uModel;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  gl_Position = uProjection * uView * uModel * aPosition;\n" +
            "  vUV = aUV;\n" +
            "}\n";

    private static final String FSHADER_SRC =
        "precision mediump float;\n" +
            "varying vec2 vUV;\n" +
            "uniform sampler2D uTex;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTex, vUV);\n" +
            "}\n";

    public static final float Z_NEAR = 0.1f;
    public static final float Z_FAR = 1000.0f;

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected boolean mIsStereo;
    protected StereoImageType mStereoType = StereoImageType.HALF;
    private Camera mCamera = new Camera();
    private boolean mFlipVertical;

    private UVSphere mShell;

    protected Bitmap mTexture;
    protected boolean mTextureUpdate = false;
    private int[] mTextures = new int[1];

    private int mPositionHandle;
    private int mProjectionMatrixHandle;
    private int mViewMatrixHandle;
    private int mUVHandle;
    private int mTexHandle;
    private int mModelMatrixHandle;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    
    private final Object mLockObj = new Object();
    private boolean mIsWaitingSnapshot;
    private byte[] mSnapshot;

    private boolean mIsScreenSizeMutable;
    private boolean mIsDestroyTextureOnUpdate;
    private SurfaceListener mSurfaceListener;

    /**
     * Constructor.
     */
    public SphericalViewRenderer() {
        mShell = new UVSphere(DEFAULT_TEXTURE_SHELL_RADIUS, SHELL_DIVIDES);
    }

    public void setDestroyTextureOnUpdate(boolean flag) {
        mIsDestroyTextureOnUpdate = flag;
    }

    public void setSurfaceListener(final SurfaceListener listener) {
        mSurfaceListener = listener;
    }

    public void setFlipVertical(final boolean isFlip) {
        mFlipVertical = isFlip;
    }

    public void setStereoImageType(final StereoImageType type) {
        mStereoType = type;
    }

    public byte[] takeSnapshot() {
        synchronized (mLockObj) {
            mIsWaitingSnapshot = true;
            try {
                mLockObj.wait();
            } catch (InterruptedException e) {
                return null;
            }
            mIsWaitingSnapshot = false;
            return mSnapshot;
        }
    }

    public int getOutputWidth() {
        return (!mIsStereo || mStereoType == StereoImageType.HALF)
            ? getScreenWidth() : getScreenWidth() * 2;
    }

    public int getOutputHeight() {
        return getScreenHeight();
    }

    private void readPixelBuffer() {
        int w = getOutputWidth();
        int h = getOutputHeight();
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
        int offset1, offset2;
        for (int i = 0; i < h; i++) {
            offset1 = i * w;
            offset2 = (h - i - 1) * w;
            for (int j = 0; j < w; j++) {
                int texturePixel = bitmapBuffer[offset1 + j];
                int blue = (texturePixel >> 16) & 0xff;
                int red = (texturePixel << 16) & 0x00ff0000;
                int pixel = (texturePixel & 0xff00ff00) | red | blue;
                bitmapSource[offset2 + j] = pixel;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        mSnapshot = baos.toByteArray();
    }

    /**
     * onDrawFrame Method
     *
     * @param gl10 GL10 Object
     */
    @Override
    public void onDrawFrame(final GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int width = getOutputWidth();
        int height = getOutputHeight();
        if (mIsStereo) {
            Camera[] cameras = mCamera.getCamerasForStereo(DISTANCE_EYES);
            int halfWidth = width / 2;
            GLES20.glViewport(0, 0, halfWidth, height);
            draw(cameras[0]);
            GLES20.glViewport(halfWidth, 0, halfWidth, height);
            draw(cameras[1]);
        } else {
            GLES20.glViewport(0, 0, width, height);
            draw(mCamera);
        }

        synchronized (mLockObj) {
            if (mIsWaitingSnapshot) {
                readPixelBuffer();
                mLockObj.notifyAll();
            }
        }
    }

    public void requestToUpdateTexture() {
        mTextureUpdate = true;
    }

    private synchronized void draw(final Camera camera) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);

        if (mTextureUpdate && null != mTexture && !mTexture.isRecycled()) {
            if (mTextures[0] != 0) {
                GLES20.glDeleteTextures(1, mTextures, 0);
            }
            loadTexture(mTexture);
            mTextureUpdate = false;
        }

        float x = camera.getPosition().x();
        float y = camera.getPosition().y();
        float z = camera.getPosition().z();
        float frontX = camera.getFrontDirection().x();
        float frontY = camera.getFrontDirection().y();
        float frontZ = camera.getFrontDirection().z();
        float upX = camera.getUpperDirection().x();
        float upY = camera.getUpperDirection().y();
        float upZ = camera.getUpperDirection().z();
        float fov = camera.mFovDegree;

        if (mFlipVertical) {
            frontY *= -1;
        }

        Matrix.setLookAtM(mViewMatrix, 0, x, y, z, frontX, frontY, frontZ, upX, upY, upZ);
        Matrix.perspectiveM(mProjectionMatrix, 0, fov, getScreenAspect(), Z_NEAR, Z_FAR);

        GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);
        GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLES20.glUniform1i(mTexHandle, 0);

        mShell.draw(mPositionHandle, mUVHandle);
    }

    /**
     * onSurfaceChanged Method
     * @param gl10 GLObject (not used)
     * @param width Screen width
     * @param height Screen height
     */
    @Override
    public void onSurfaceChanged(final GL10 gl10, final int width, final int height) {
        if (!isScreenSizeMutable()) {
            mScreenWidth = width;
            mScreenHeight = height;
        }
    }

    /**
     * onSurfaceCreated Method
     * @param gl10 GLObject
     * @param config EGL Setting Object
     */
    @Override
    public void onSurfaceCreated(final GL10 gl10, final EGLConfig config) {
        int vShader = loadShader(GLES20.GL_VERTEX_SHADER, VSHADER_SRC);
        int fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FSHADER_SRC);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        mPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        mUVHandle = GLES20.glGetAttribLocation(program, "aUV");
        mProjectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjection");
        mViewMatrixHandle = GLES20.glGetUniformLocation(program, "uView");
        mTexHandle = GLES20.glGetUniformLocation(program, "uTex");
        mModelMatrixHandle = GLES20.glGetUniformLocation(program, "uModel");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private float getScreenAspect() {
        int width = mIsStereo && mStereoType == StereoImageType.HALF ? mScreenWidth / 2 : mScreenWidth;
        return (float) width / (float) (mScreenHeight == 0 ? 1 : mScreenHeight);
    }

    /**
     * Sets the texture for the sphere
     *
     * @param texture Photo object for texture
     */
    public synchronized void setTexture(final Bitmap texture) {
        if (mTexture != null && mIsDestroyTextureOnUpdate) {
            try {
                mTexture.recycle();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        mTexture = texture;
        mTextureUpdate = true;
    }

    /**
     * Acquires the set texture
     *
     * @return Photo object for texture
     */
    public Bitmap getTexture() {
        return mTexture;
    }
    
    /**
     * GL error judgment method for debugging
     * @param glOperation Message output character string
     */
    private void checkGlError(final String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            mLogger.warning(glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Texture setting method
     *
     * @param texture Setting texture
     */
    public void loadTexture(final Bitmap texture) {
        GLES20.glGenTextures(1, mTextures, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
    }

    private int loadShader(final int type, final String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public boolean isStereo() {
        return mIsStereo;
    }

    public void setScreenSettings(final int width, final int height, final boolean isStereo) {
        boolean isChanged = false;
        if (isScreenSizeMutable()) {
            if (mScreenWidth != width || mScreenHeight != height) {
                isChanged = true;
            }
            mScreenWidth = width;
            mScreenHeight = height;
        }

        if (mIsStereo != isStereo) {
            isChanged = true;
        }
        mIsStereo = isStereo;

        if (isChanged && mSurfaceListener != null) {
            mSurfaceListener.onSurfaceChanged(width, height, isStereo);
        }
    }

    public void setScreenSizeMutable(final boolean isMutable) {
        mIsScreenSizeMutable = isMutable;
    }

    public boolean isScreenSizeMutable() {
        return mIsScreenSizeMutable;
    }

    public void setSphereRadius(final float radius) {
        if (radius != mShell.getRadius()) {
            mShell = new UVSphere(radius, SHELL_DIVIDES);
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(final Camera camera) {
        mCamera = camera;
    }

    public static class CameraBuilder {
        private float mFovDegree;
        private Vector3D mPosition;
        private Vector3D mFrontDirection;
        private Vector3D mUpperDirection;
        private Vector3D mRightDirection;
        private Quaternion mAttitude;

        public CameraBuilder(final Camera camera) {
            mFovDegree = camera.mFovDegree;
            mPosition = new Vector3D(camera.mPosition);
            mFrontDirection = new Vector3D(camera.mFrontDirection);
            mUpperDirection = new Vector3D(camera.mUpperDirection);
            mRightDirection = new Vector3D(camera.mRightDirection);
            if (camera.mAttitude != null) {
                mAttitude = new Quaternion(camera.mAttitude);
            }
        }

        public CameraBuilder() {
            this(new Camera());
        }

        public Camera create() {
            Camera camera = new Camera(mFovDegree, mPosition,
                mFrontDirection,
                mUpperDirection,
                mRightDirection,
                mAttitude);
            return camera;
        }

        public void setFov(float degree) {
            mFovDegree = degree;
        }

        public void setPosition(final Vector3D p) {
            mPosition = p;
        }

        public void slideHorizontal(final float delta) {
            mPosition = new Vector3D(
                delta * mRightDirection.x() + mPosition.x(),
                delta * mRightDirection.y() + mPosition.y(),
                delta * mRightDirection.z() + mPosition.z()
            );
        }

        public void rotateByEulerAngle(final float roll, final float yaw, final float pitch) {
            Vector3D lastFrontDirection = mFrontDirection;

            float lat = (90.0f - pitch);
            float lng = yaw;
            float x = (float) (Math.sin(lat) * Math.cos(lng));
            float y = (float) (Math.cos(lat));
            float z = (float) (Math.sin(lat) * Math.sin(lng));
            mFrontDirection = new Vector3D(x, y, z);

            float dx = mFrontDirection.x() - lastFrontDirection.x();
            float dy = mFrontDirection.y() - lastFrontDirection.y();
            float dz = mFrontDirection.z() - lastFrontDirection.z();

            float theta = roll;
            Quaternion q = new Quaternion(
                (float) Math.cos(theta / 2.0f),
                mFrontDirection.multiply((float) Math.sin(theta / 2.0f))
            );
            mUpperDirection = rotate(mUpperDirection, q);
            mRightDirection = mRightDirection.add(new Vector3D(dx, dy, dz));
            mRightDirection = rotate(mRightDirection, q);
        }

        public void rotate(final Quaternion q) {
            mFrontDirection = rotate(new Vector3D(1, 0, 0), q);
//            mUpperDirection = rotate(new Vector3D(0, 1, 0), q);
            mRightDirection = rotate(new Vector3D(0, 0, 1), q);
        }

        private static Vector3D rotate(final Vector3D v, final Quaternion q) {
            Quaternion p = new Quaternion(0, v);
            Quaternion r = q.conjugate();
            Quaternion qpr = r.multiply(p).multiply(q);
            return qpr.imaginary();
        }
    }

    public static class Camera {
        private final float mFovDegree;
        private final Vector3D mPosition;
        private final Vector3D mFrontDirection;
        private final Vector3D mUpperDirection;
        private final Vector3D mRightDirection;
        private final Quaternion mAttitude;

        public Camera(final float fovDegree, final Vector3D position,
                      final Vector3D frontDirection, final Vector3D upperDirection,
                      final Vector3D rightDirection,
                      final Quaternion attitude) {
            mFovDegree = fovDegree;
            mPosition = position;
            mFrontDirection = frontDirection;
            mUpperDirection = upperDirection;
            mRightDirection = rightDirection;
            mAttitude = attitude;
        }

        public Camera() {
            this(90, new Vector3D(0.0f, 0.0f, 0.0f),
                new Vector3D(1.0f, 0.0f, 0.0f),
                new Vector3D(0.0f, 1.0f, 0.0f),
                new Vector3D(0.0f, 0.0f, 1.0f),
                Quaternion.quaternionFromAxisAndAngle(new Vector3D(1.0f, 0.0f, 0.0f), 0));
        }

        public Vector3D getPosition() {
            return mPosition;
        }

        public Vector3D getFrontDirection() {
            return mFrontDirection;
        }

        public Vector3D getUpperDirection() {
            return mUpperDirection;
        }

        public Vector3D getRightDirection() {
            return mRightDirection;
        }

        public Camera[] getCamerasForStereo(final float distance) {
            CameraBuilder leftCamera = new CameraBuilder(this);
            leftCamera.slideHorizontal(-1 * (distance / 2.0f));
            CameraBuilder rightCamera = new CameraBuilder(this);
            rightCamera.slideHorizontal((distance / 2.0f));

            return new Camera[] {
                leftCamera.create(),
                rightCamera.create()
            };
        }
    }

    public interface SurfaceListener {

        void onSurfaceChanged(final int width, final int height, final boolean isStereo);

    }

    public enum StereoImageType {
        HALF,
        DOUBLE
    }
}
