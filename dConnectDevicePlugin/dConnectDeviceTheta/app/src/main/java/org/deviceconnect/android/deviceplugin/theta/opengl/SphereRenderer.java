package org.deviceconnect.android.deviceplugin.theta.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.opengl.model.UVSphere;

import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;
import org.restlet.representation.StringRepresentation;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Renderer class for photo display.
 */
public class SphereRenderer implements Renderer {

    /** Distance of left and right eye: {@value} cm. */
    private static final float DISTANCE_EYES = 10.0f / 100.0f;
    /** Radius of sphere for photo. */
    private static final float DEFAULT_TEXTURE_SHELL_RADIUS = 1.0f;
    /** Number of sphere polygon partitions for photo, which must be an even number. */
    private static final int SHELL_DIVIDES = 40;

    private final String VSHADER_SRC =
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

    private final String FSHADER_SRC =
            "precision mediump float;\n" +
            "varying vec2 vUV;\n" +
            "uniform sampler2D uTex;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTex, vUV);\n" +
            "}\n";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;

    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsStereo;
    private Camera mCamera = new Camera();

    private UVSphere mShell;

    private Bitmap mTexture;
    private boolean mTextureUpdate = false;
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

    /**
     * Constructor
     */
    public SphereRenderer() {
        mShell = new UVSphere(DEFAULT_TEXTURE_SHELL_RADIUS, SHELL_DIVIDES);
    }

    /**
     * onDrawFrame Method
     * @param gl GLObject (not used)
     */
    @Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mIsStereo) {
            SphereRenderer.Camera[] cameras = mCamera.getCamerasForStereo(DISTANCE_EYES);
            GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
            draw(cameras[0]);
            GLES20.glViewport(mScreenWidth, 0, mScreenWidth, mScreenHeight);
            draw(cameras[1]);
        } else {
            GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
            draw(mCamera);
        }
    }

    private void draw(final Camera camera) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);

        if (mTextureUpdate && null != mTexture && !mTexture.isRecycled()) {
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
        Matrix.setLookAtM(mViewMatrix, 0, x, y, z, frontX, frontY, frontZ, upX, upY, upZ);
        Matrix.perspectiveM(mProjectionMatrix, 0, camera.mFovDegree, getScreenAspect(), Z_NEAR, Z_FAR);

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
     * @param gl GLObject (not used)
     * @param width Screen width
     * @param height Screen height
     */
    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
    }

    /**
     * onSurfaceCreated Method
     * @param gl GLObject (not used)
     * @param config EGL Setting Object
     */
    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

        int vShader;
        int fShader;
        int program;

        vShader = loadShader(GLES20.GL_VERTEX_SHADER, VSHADER_SRC);
        fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FSHADER_SRC);

        program = GLES20.glCreateProgram();
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
        return (float) mScreenWidth / (float) (mScreenHeight == 0 ? 1 : mScreenHeight);
    }

    /**
     * Sets the texture for the sphere
     * @param texture Photo object for texture
     */
    public void setTexture(Bitmap texture) {
        mTexture = texture;
        mTextureUpdate = true;
    }

    /**
     * Acquires the set texture
     * @return Photo object for texture
     */
    public Bitmap getTexture() {
        return mTexture;
    }


    /**
     * GL error judgment method for debugging
     * @param TAG TAG output character string
     * @param glOperation Message output character string
     */
    public static void checkGlError(String TAG, String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Texture setting method
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

    private int loadShader(final int type, final String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void setScreenWidth(final int screenWidth) {
        mScreenWidth = screenWidth;
    }

    public void setScreenHeight(final int screenHeight) {
        mScreenHeight = screenHeight;
    }

    public void setSphereRadius(final float radius) {
        if (radius != mShell.getRadius()) {
            mShell = new UVSphere(radius, SHELL_DIVIDES);
        }
    }

    public void setStereoMode(final boolean isStereo) {
        mIsStereo = isStereo;
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
            float radianPerDegree = (float) (Math.PI / 180.0);

            float lat = (90.0f - pitch) * radianPerDegree;
            float lng = yaw * radianPerDegree;
            float x = (float) (Math.sin(lat) * Math.cos(lng));
            float y = (float) (Math.cos(lat));
            float z = (float) (Math.sin(lat) * Math.sin(lng));
            mFrontDirection = new Vector3D(x, y, z);

            float dx = mFrontDirection.x() - lastFrontDirection.x();
            float dy = mFrontDirection.y() - lastFrontDirection.y();
            float dz = mFrontDirection.z() - lastFrontDirection.z();

            float theta = roll * radianPerDegree;
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
            mUpperDirection = rotate(new Vector3D(0, 1, 0), q);
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
}