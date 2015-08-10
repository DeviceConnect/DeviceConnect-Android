package org.deviceconnect.android.deviceplugin.theta.roi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.opengl.SphereRenderer;

import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;
import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoiDeliveryContext implements SensorEventListener  {

    public static final Param DEFAULT_PARAM = new Param();

    private static final float NS2S = 1.0f / 1000000000.0f;

    private final float[] mDeltaRotationVector = new float[4];

    private long mLastEventTimestamp;

    private final OmnidirectionalImage mSource;

    private final SensorManager mSensorMgr;

    private PixelBuffer mPixelBuffer;

    private final SphereRenderer mRenderer = new SphereRenderer();

    private Param mCurrentParam = DEFAULT_PARAM;

    private Uri mUri;

    private String mSegment;

    private byte[] mRoi = null;

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);

    public RoiDeliveryContext(final Context context, final OmnidirectionalImage source) {
        mSource = source;
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public String getSegment() {
        return mSegment;
    }

    public byte[] getRoi() {
        return mRoi;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(final Uri uri) {
        mUri = uri;
        mSegment = uri.getLastPathSegment();
    }

    public Param getCurrentParam() {
        return mCurrentParam;
    }

    public void destroy() {
        if (mPixelBuffer != null) {
            mPixelBuffer.destroy();
        }
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this);
        }
    }

    public void render(final boolean isUserRequest) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int width = mCurrentParam.getImageWidth();
                int height = mCurrentParam.getImageHeight();

                Bitmap result;
                if (mCurrentParam.isStereoMode()) {
                    float distance = 2.5f / 100.0f; // 5cm
                    SphereRenderer.Camera[] cameras = mRenderer.getCamera().getCamerasForStereo(distance);

                    mRenderer.setCamera(cameras[0]);
                    Bitmap left = mPixelBuffer.render();
                    mRenderer.setCamera(cameras[1]);
                    Bitmap right = mPixelBuffer.render();

                    Bitmap stereo = Bitmap.createBitmap(2 * width, height, Bitmap.Config.ARGB_8888);
                    Canvas offScreen = new Canvas(stereo);
                    offScreen.drawBitmap(left, 0, 0, null);
                    offScreen.drawBitmap(right, width, 0, null);
                    left.recycle();
                    right.recycle();
                    result = stereo;
                } else {
                    result = mPixelBuffer.render();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] roi = baos.toByteArray();
                result.recycle();

                mRoi = roi;
                if (mListener != null) {
                    mListener.onUpdate(mSegment, roi);
                }
            }
        });
    }

    public void changeRendererParam(final Param param, final boolean isUserRequest) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int width = param.getImageWidth();
                int height = param.getImageHeight();

                if (isUserRequest) {
                    if (mPixelBuffer == null) {
                        mPixelBuffer = new PixelBuffer(width, height);
                        mRenderer.setTexture(mSource.getData());
                        mPixelBuffer.setRenderer(mRenderer);
                    } else if (width != mCurrentParam.getImageWidth() || height != mCurrentParam.getImageHeight()) {
                        mPixelBuffer.destroy();
                        mPixelBuffer = new PixelBuffer(width, height);
                        mRenderer.setTexture(mSource.getData());
                        mPixelBuffer.setRenderer(mRenderer);
                    }
                    if (param.isVrMode()) {
                        startVrMode();
                    } else {
                        stopVrMode();
                    }
                }

                mCurrentParam = param;

                SphereRenderer.CameraBuilder builder = new SphereRenderer.CameraBuilder();
                builder.setPosition(new Vector3D(
                    (float) param.getCameraX(),
                    (float) param.getCameraY(),
                    (float) param.getCameraZ()));
                if (isUserRequest) {
                    builder.rotateByByEulerAngle(
                        (float) param.getCameraRoll(),
                        (float) param.getCameraPitch(),
                        (float) param.getCameraYaw());
                }
                builder.setFov((float) param.getCameraFov());
                mRenderer.setCamera(builder.create());
                mRenderer.setSphereRadius((float) param.getSphereSize());
                mRenderer.setScreenWidth(param.getImageWidth());
                mRenderer.setScreenHeight(param.getImageHeight());
            }
        });
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE) {
            return;
        }
        if (!mCurrentParam.isVrMode()) {
            return;
        }

        if (mLastEventTimestamp != 0) {
            final float dT = (event.timestamp - mLastEventTimestamp) * NS2S;
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > 0) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            mDeltaRotationVector[0] = sinThetaOverTwo * axisX;
            mDeltaRotationVector[1] = sinThetaOverTwo * axisY;
            mDeltaRotationVector[2] = sinThetaOverTwo * axisZ;
            mDeltaRotationVector[3] = cosThetaOverTwo;

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Quaternion q = new Quaternion(
                        mDeltaRotationVector[3],
                        new Vector3D(
                            mDeltaRotationVector[2],
                            mDeltaRotationVector[1],
                            mDeltaRotationVector[0]
                        )
                    );
                    mRenderer.rotateCamera(q);
                    render(false);
                }
            });
        }
        mLastEventTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do.
    }

    private boolean startVrMode() {
        List<Sensor> sensors = mSensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            return true;
        } else {
            return false;
        }
    }

    private void stopVrMode() {
        mSensorMgr.unregisterListener(this);
    }

    private OnChangeListener mListener;

    public void setOnChangeListener(final OnChangeListener listener) {
        mListener = listener;
    }

    public static interface OnChangeListener {
        void onUpdate(String segment, byte[] roi);
    }

    public static class Param {

        double mCameraX;

        double mCameraY;

        double mCameraZ;

        double mCameraYaw;

        double mCameraRoll;

        double mCameraPitch;

        double mCameraFov = 90;

        double mSphereSize = 1.0d;

        int mImageWidth = 480;

        int mImageHeight = 270;

        boolean mStereoMode;

        boolean mVrMode;

        public double getCameraX() {
            return mCameraX;
        }

        public void setCameraX(final double x) {
            mCameraX = x;
        }

        public double getCameraY() {
            return mCameraY;
        }

        public void setCameraY(final double y) {
            mCameraY = y;
        }

        public double getCameraZ() {
            return mCameraZ;
        }

        public void setCameraZ(final double z) {
            mCameraZ = z;
        }

        public double getCameraYaw() {
            return mCameraYaw;
        }

        public void setCameraYaw(final double yaw) {
            mCameraYaw = yaw;
        }

        public void addCameraYaw(final double delta) {
            mCameraYaw += delta;
            if (mCameraYaw >= 360) {
                mCameraYaw -= 360;
            }
        }

        public double getCameraRoll() {
            return mCameraRoll;
        }

        public void setCameraRoll(final double roll) {
            mCameraRoll = roll;
        }

        public void addCameraRoll(final double delta) {
            mCameraRoll += delta;
            if (mCameraRoll >= 360) {
                mCameraRoll -= 360;
            }
        }

        public double getCameraPitch() {
            return mCameraPitch;
        }

        public void setCameraPitch(final double pitch) {
            mCameraPitch = pitch;
        }

        public void addCameraPitch(final double delta) {
            mCameraPitch += delta;
            if (mCameraPitch >= 360) {
                mCameraPitch -= 360;
            }
        }

        public double getCameraFov() {
            return mCameraFov;
        }

        public void setCameraFov(final double fov) {
            mCameraFov = fov;
        }

        public double getSphereSize() {
            return mSphereSize;
        }

        public void setSphereSize(final double size) {
            mSphereSize = size;
        }

        public int getImageWidth() {
            return mImageWidth;
        }

        public void setImageWidth(final int width) {
            mImageWidth = width;
        }

        public int getImageHeight() {
            return mImageHeight;
        }

        public void setImageHeight(final int height) {
            mImageHeight = height;
        }

        public boolean isStereoMode() {
            return mStereoMode;
        }

        public void setStereoMode(final boolean isStereo) {
            mStereoMode = isStereo;
        }

        public boolean isVrMode() {
            return mVrMode;
        }

        public void setVrMode(final boolean isVr) {
            mVrMode = isVr;
        }
    }
}
