/*
 RoiDeliveryContext.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.roi;


import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;
import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * ROI Image Delivery Context.
 *
 * @author NTT DOCOMO, INC.
 */
public class RoiDeliveryContext implements SensorEventListener {

    /**
     * The default parameter of ROI Settings API.
     */
    public static final Param DEFAULT_PARAM = new Param();

    private static final float NS2S = 1.0f / 1000000000.0f;

    private static final long EXPIRE_INTERVAL = 10 * 1000;

    private long mLastEventTimestamp;

    private float mEventInterval;

    private int mDisplayRotation;

    private final OmnidirectionalImage mSource;

    private final SensorManager mSensorMgr;

    private Timer mExpireTimer;

    private Timer mDeliveryTimer;

    private PixelBuffer mPixelBuffer;

    private final SphericalViewRenderer mRenderer = new SphericalViewRenderer();

    private Param mCurrentParam = DEFAULT_PARAM;

    private String mUri;

    private String mSegment;

    private byte[] mRoi = null;

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);

    private ByteArrayOutputStream mBaos;

    private OnChangeListener mListener;

    private Quaternion mCurrentRotation = new Quaternion(1, new Vector3D(0, 0, 0));

    private float[] mCurrentGyroscope = new float[3];

    private boolean mInitFlag = false;

    private Logger mLogger = Logger.getLogger("theta.dplugin");

    /**
     * Constructor.
     *
     * @param context an instance of {@link Context}
     * @param source an instance of {@link OmnidirectionalImage} to create ROI image
     */
    public RoiDeliveryContext(final Context context, final OmnidirectionalImage source) {
        WindowManager windowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplayRotation = windowMgr.getDefaultDisplay().getRotation();

        mSource = source;
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mBaos = new ByteArrayOutputStream(
            mCurrentParam.getImageWidth() * mCurrentParam.getImageHeight());
    }

    public byte[] getRoi() {
        return mRoi;
    }

    public void setUri(final String uriString) {
        mUri = uriString;
        mSegment = Uri.parse(uriString).getLastPathSegment();
    }

    public String getUri() {
        return mUri;
    }

    public String getSegment() {
        return mSegment;
    }

    public void destroy() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                stopDeliveryTimer();
                if (mPixelBuffer != null) {
                    mPixelBuffer.destroy();
                }
                if (mSensorMgr != null) {
                    mSensorMgr.unregisterListener(RoiDeliveryContext.this);
                }
            }
        });
    }

    public byte[] renderWithBlocking() {
        Future<byte[]> future = mExecutor.submit(new RenderingTask());
        try {
            return future.get();
        } catch (ExecutionException e) {
            // Nothing to do.
        } catch (InterruptedException e) {
            // Nothing to do.
        }
        return null;
    }

    public void render() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    new RenderingTask().call();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e("AAA", "render: ", e);
                    }
                    // Nothing to do.
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
                        mPixelBuffer = new PixelBuffer(width, height, param.isStereoMode());
                        mRenderer.setTexture(mSource.getData());
                        mPixelBuffer.setRenderer(mRenderer);
                        mBaos = new ByteArrayOutputStream(width * height);
                    } else if (isDisplaySizeChanged(param)) {
                        mPixelBuffer.destroy();
                        mPixelBuffer = new PixelBuffer(width, height, param.isStereoMode());
                        mRenderer.setTexture(mSource.getData());
                        mPixelBuffer.setRenderer(mRenderer);
                        mBaos = new ByteArrayOutputStream(width * height);
                    }
                    if (param.isVrMode()) {
                        startVrMode();
                    } else {
                        stopVrMode();
                    }
                }

                mCurrentParam = param;

                SphericalViewRenderer.CameraBuilder builder = new SphericalViewRenderer.CameraBuilder();
                builder.setPosition(new Vector3D(
                    (float) param.getCameraX(),
                    (float) param.getCameraY() * -1,
                    (float) param.getCameraZ()));
                if (isUserRequest) {
                    builder.rotateByEulerAngle(
                        (float) param.getCameraRoll(),
                        (float) param.getCameraYaw(),
                        (float) param.getCameraPitch() * -1);
                }
                builder.setFov((float) param.getCameraFov());
                mRenderer.setCamera(builder.create());
                mRenderer.setSphereRadius((float) param.getSphereSize());
                if (param.isStereoMode()) {
                    mRenderer.setScreenWidth(width * 2);
                } else {
                    mRenderer.setScreenWidth(width);
                }
                mRenderer.setScreenHeight(param.getImageHeight());
                mRenderer.setStereoMode(param.isStereoMode());
            }
        });
    }

    private boolean isDisplaySizeChanged(final Param newParam) {
        return newParam.isStereoMode() != mCurrentParam.isStereoMode()
            || newParam.getImageWidth() != mCurrentParam.getImageWidth()
            || newParam.getImageHeight() != mCurrentParam.getImageHeight();
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (mLastEventTimestamp != 0) {
            float epsilon = 0.000000001f;
            float[] vGyroscope = new float[3];
            float[] deltaVGyroscope = new float[4];
            Quaternion qGyroscopeDelta;
            float dT = (event.timestamp - mLastEventTimestamp) * NS2S;

            final float alpha = 0.8f;
            if (!mInitFlag) {
                System.arraycopy(event.values, 0, vGyroscope, 0, vGyroscope.length);
                System.arraycopy(event.values, 0, mCurrentGyroscope, 0, event.values.length);
                mInitFlag = true;
            } else {
                vGyroscope[0] = alpha * mCurrentGyroscope[0] + (1.0f - alpha) * event.values[0];
                vGyroscope[1] = alpha * mCurrentGyroscope[1] + (1.0f - alpha) * event.values[1];
                vGyroscope[2] = alpha * mCurrentGyroscope[2] + (1.0f - alpha) * event.values[2];
                System.arraycopy(vGyroscope, 0, mCurrentGyroscope, 0, vGyroscope.length);
            }

            float tmp = vGyroscope[2];
            vGyroscope[2] = vGyroscope[0] * -1;
            vGyroscope[0] = tmp;

            float magnitude = (float) Math.sqrt(Math.pow(vGyroscope[0], 2)
                + Math.pow(vGyroscope[1], 2) + Math.pow(vGyroscope[2], 2));
            if (magnitude > epsilon) {
                vGyroscope[0] /= magnitude;
                vGyroscope[1] /= magnitude;
                vGyroscope[2] /= magnitude;
            }

            float thetaOverTwo = magnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

            deltaVGyroscope[0] = sinThetaOverTwo * vGyroscope[0];
            deltaVGyroscope[1] = sinThetaOverTwo * vGyroscope[1];
            deltaVGyroscope[2] = sinThetaOverTwo * vGyroscope[2];
            deltaVGyroscope[3] = cosThetaOverTwo;

            float[] delta = new float[3];
            switch (mDisplayRotation) {
                case Surface.ROTATION_0:
                    delta[0] = deltaVGyroscope[0];
                    delta[1] = deltaVGyroscope[1];
                    delta[2] = deltaVGyroscope[2];
                    break;
                case Surface.ROTATION_90:
                    delta[0] = deltaVGyroscope[0];
                    delta[1] = deltaVGyroscope[2] * -1;
                    delta[2] = deltaVGyroscope[1];
                    break;
                case Surface.ROTATION_180:
                    delta[0] = deltaVGyroscope[0];
                    delta[1] = deltaVGyroscope[1] * -1;
                    delta[2] = deltaVGyroscope[2];
                    break;
                case Surface.ROTATION_270:
                    delta[0] = deltaVGyroscope[0];
                    delta[1] = deltaVGyroscope[2];
                    delta[2] = deltaVGyroscope[1] * -1;
                    break;
                default:
                    break;
            }

            qGyroscopeDelta = new Quaternion(deltaVGyroscope[3], new Vector3D(delta));

            mCurrentRotation = qGyroscopeDelta.multiply(mCurrentRotation);

            float[] qvOrientation = new float[4];
            qvOrientation[0] = mCurrentRotation.imaginary().x();
            qvOrientation[1] = mCurrentRotation.imaginary().y();
            qvOrientation[2] = mCurrentRotation.imaginary().z();
            qvOrientation[3] = mCurrentRotation.real();

            float[] rmGyroscope = new float[9];
            SensorManager.getRotationMatrixFromVector(rmGyroscope,
                qvOrientation);

            float[] vOrientation = new float[3];
            SensorManager.getOrientation(rmGyroscope, vOrientation);

            SphericalViewRenderer.Camera currentCamera = mRenderer.getCamera();
            SphericalViewRenderer.CameraBuilder newCamera = new SphericalViewRenderer.CameraBuilder(currentCamera);
            newCamera.rotate(mCurrentRotation);
            mRenderer.setCamera(newCamera.create());

            mEventInterval += dT;
            if (mEventInterval >= 0.1f) {
                mEventInterval = 0;
                render();
            }
        }
        mLastEventTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do.
    }

    private boolean startVrMode() {
        // Reset current rotation.
        mCurrentRotation = new Quaternion(1, new Vector3D(0, 0, 0));

        List<Sensor> sensors = mSensorMgr.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() == 0) {
            mLogger.warning("Failed to start VR mode: any sensor is NOT found.");
            return false;
        }
        for (Sensor sensor : sensors) {
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mInitFlag = false;
                mCurrentGyroscope[0] = 0.0f;
                mCurrentGyroscope[1] = 0.0f;
                mCurrentGyroscope[2] = 0.0f;

                mLogger.info("Started VR mode: GYROSCOPE sensor is found.");
                mSensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                return true;
            }
        }
        mLogger.warning("Failed to start VR mode: GYROSCOPE sensor is NOT found.");
        return false;
    }

    private void stopVrMode() {
        mSensorMgr.unregisterListener(this);
    }

    public void startExpireTimer() {
        if (mExpireTimer != null) {
            return;
        }
        long now = System.currentTimeMillis();
        Date expireTime = new Date(now + EXPIRE_INTERVAL);
        mExpireTimer = new Timer();
        mExpireTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mExpireTimer.cancel();
                mExpireTimer = null;
                if (mListener != null) {
                    mListener.onExpire(RoiDeliveryContext.this);
                }
            }
        }, expireTime);
    }

    public void stopExpireTimer() {
        if (mExpireTimer != null) {
            mExpireTimer.cancel();
            mExpireTimer = null;
        }
    }

    public void restartExpireTimer() {
        stopExpireTimer();
        startExpireTimer();
    }

    public void startDeliveryTimer() {
        if (mDeliveryTimer != null) {
            return;
        }
        mDeliveryTimer = new Timer();
        mDeliveryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onUpdate(RoiDeliveryContext.this, getRoi());
                }
            }
        }, 250, 1000);
    }

    public void stopDeliveryTimer() {
        if (mDeliveryTimer != null) {
            mDeliveryTimer.cancel();
            mDeliveryTimer = null;
        }
    }

    public void setOnChangeListener(final OnChangeListener listener) {
        mListener = listener;
    }

    public static interface OnChangeListener {

        void onUpdate(RoiDeliveryContext roiContext, byte[] roi);

        void onExpire(RoiDeliveryContext roiContext);

    }

    public static class Param {

        double mCameraX;

        double mCameraY;

        double mCameraZ;

        double mCameraYaw;

        double mCameraRoll;

        double mCameraPitch;

        double mCameraFov = 90.0d;

        double mSphereSize = 1.0d;

        int mImageWidth = 600;

        int mImageHeight = 400;

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

        public double getCameraRoll() {
            return mCameraRoll;
        }

        public void setCameraRoll(final double roll) {
            mCameraRoll = roll;
        }

        public double getCameraPitch() {
            return mCameraPitch;
        }

        public void setCameraPitch(final double pitch) {
            mCameraPitch = pitch;
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

    private class RenderingTask implements Callable<byte[]> {

        @Override
        public byte[] call() throws Exception {
            mPixelBuffer.render();
            Bitmap result = mPixelBuffer.convertToBitmap();

            mBaos.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 100, mBaos);
            byte[] roi = mBaos.toByteArray();

            mRoi = roi;
            if (mListener != null) {
                mListener.onUpdate(RoiDeliveryContext.this, roi);
            }
            return roi;
        }
    }
}
