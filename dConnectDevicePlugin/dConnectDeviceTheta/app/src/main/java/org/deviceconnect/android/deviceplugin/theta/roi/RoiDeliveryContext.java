package org.deviceconnect.android.deviceplugin.theta.roi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;

import org.deviceconnect.android.deviceplugin.theta.opengl.SphereRenderer;

import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class RoiDeliveryContext implements SensorEventListener  {

    public static final Param DEFAULT_PARAM = new Param();

    private static final long ROI_DELIVERY_INTERVAL = 200;

    private long mSensorEventTimestamp;

    private long mInterval;

    private float[] mRotationDelta = new float[3];

    private final OmnidirectionalImage mSource;

    private final SensorManager mSensorMgr;

    private byte[] mRoi;

    private Param mCurrentParam = DEFAULT_PARAM;

    private Uri mUri;

    private String mSegment;

    public RoiDeliveryContext(final Context context, final OmnidirectionalImage source) {
        mSource = source;
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(final Uri uri) {
        mUri = uri;
        mSegment = uri.getLastPathSegment();
    }

    public byte[] getRoi() {
        return mRoi;
    }

    public Param getCurrentParam() {
        return mCurrentParam;
    }

    public void render(final Param param) {
        if (param == null) {
            throw new IllegalArgumentException();
        }
        if (param.isVrMode() && !mCurrentParam.isVrMode()) {
            startVrMode();
        } else if (!param.isVrMode() && mCurrentParam.isVrMode()) {
            stopVrMode();
        }
        mCurrentParam = param;

        int width = param.getImageWidth();
        int height = param.getImageHeight();
        PixelBuffer pxBuffer = new PixelBuffer(width, height);
        Bitmap result;
        if (param.isStereoMode()) {
            // TODO: 左右で視点をずらす
            Param leftParam = param;
            Param rightParam = param;

            pxBuffer.setRenderer(createRenderer(leftParam));
            Bitmap left = pxBuffer.render();
            pxBuffer.setRenderer(createRenderer(rightParam));
            Bitmap right = pxBuffer.render();

            Bitmap stereo = Bitmap.createBitmap(2 * width, height, Bitmap.Config.ARGB_8888);
            Canvas offScreen = new Canvas(stereo);
            offScreen.drawBitmap(left, 0, 0, null);
            offScreen.drawBitmap(right, width, 0, null);
            result = stereo;
        } else {
            pxBuffer.setRenderer(createRenderer(param));
            result = pxBuffer.render();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        mRoi = baos.toByteArray();
    }

    private GLSurfaceView.Renderer createRenderer(final Param param) {
        SphereRenderer renderer = new SphereRenderer();
        renderer.setCameraPos(
            (float) param.getCameraX(),
            (float) param.getCameraY(),
            (float) param.getCameraZ());
        renderer.setCameraDirectionByEulerAngle(
            (float) param.getCameraRoll(),
            (float) param.getCameraPitch(),
            (float) param.getCameraYaw());
        renderer.setCameraFovDegree((float) param.getCameraFov());
        renderer.setScreenWidth(param.getImageWidth());
        renderer.setScreenHeight(param.getImageHeight());
        renderer.setTexture(mSource.getData());
        return renderer;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE) {
            return;
        }
        if (!mCurrentParam.isVrMode()) {
            return;
        }

        if (mSensorEventTimestamp != 0) {
            mInterval += event.timestamp - mSensorEventTimestamp;
            if (mInterval >= ROI_DELIVERY_INTERVAL) {
                mInterval = 0;
                float[] delta = new float[3];
                delta[0] = mRotationDelta[2] * 20;
                delta[1] = mRotationDelta[1] * 20;
                delta[2] = mRotationDelta[0] * 20;

                changeDirection(delta);

                mRotationDelta = new float[3];
                mSensorEventTimestamp = event.timestamp;
            }
        } else {
            mSensorEventTimestamp = event.timestamp;
        }
        mRotationDelta[0] += event.values[0];
        mRotationDelta[1] += event.values[1];
        mRotationDelta[2] += event.values[2];
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do.
    }

    private void changeDirection(final float[] delta) {
        Param param = getCurrentParam();
        param.addCameraRoll(delta[0]);
        param.addCameraPitch(delta[1]);
        param.addCameraYaw(delta[2]);
        render(param);

        if (mListener != null) {
            mListener.onUpdate(mSegment, mRoi);
        }
    }

    private boolean startVrMode() {
        List<Sensor> sensors = mSensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
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
