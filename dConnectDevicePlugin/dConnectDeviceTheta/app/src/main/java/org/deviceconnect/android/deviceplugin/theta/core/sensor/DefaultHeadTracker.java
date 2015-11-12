package org.deviceconnect.android.deviceplugin.theta.core.sensor;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

import java.util.logging.Logger;

public class DefaultHeadTracker extends AbstractHeadTracker implements SensorEventListener {

    private static final float NS2S = 1.0f / 1000000000.0f;

    private long mLastEventTimestamp;

    private final SensorManager mSensorMgr;

    private Quaternion mCurrentRotation = new Quaternion(1, new Vector3D(0, 0, 0));

    private float[] mCurrentGyroscope = new float[3];

    private boolean mInitFlag = false;

    private Logger mLogger = Logger.getLogger("theta.dplugin");

    private final WindowManager mWindowMgr;

    public DefaultHeadTracker(final Context context) {
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mWindowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void start() {
        // Reset current rotation.
        mCurrentRotation = new Quaternion(1, new Vector3D(0, 0, 0));

        Sensor sensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sensor == null) {
            mLogger.warning("Failed to start: Default GYROSCOPE sensor is NOT found.");
            return;
        }
        mSensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        mInitFlag = false;
        for (int i = 0; i < mCurrentGyroscope.length; i++) {
            mCurrentGyroscope[i] = 0.0f;
        }
    }

    @Override
    public void stop() {
        mSensorMgr.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do.
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (mLastEventTimestamp != 0) {
            float[] values = new float[3];
            int displayOrientation = mWindowMgr.getDefaultDisplay().getRotation();
            switch (displayOrientation) {
                case Surface.ROTATION_0:
                    values[0] = event.values[0];
                    values[1] = event.values[1];
                    values[2] = event.values[2];
                    break;
                case Surface.ROTATION_90:
                    values[0] = event.values[1] * -1;
                    values[1] = event.values[0];
                    values[2] = event.values[2];
                    break;
                case Surface.ROTATION_180:
                    values[0] = event.values[0] * -1;
                    values[1] = event.values[1] * -1;
                    values[2] = event.values[2];
                    break;
                case Surface.ROTATION_270:
                    values[0] = event.values[1];
                    values[1] = event.values[0] * -1;
                    values[2] = event.values[2];
                    break;
                default:
                    break;
            }

            float epsilon = 0.000000001f;
            float[] vGyroscope = new float[3];
            float[] deltaVGyroscope = new float[4];
            Quaternion qGyroscopeDelta;
            float dT = (event.timestamp - mLastEventTimestamp) * NS2S;

            final float alpha = 0.8f;
            if (!mInitFlag) {
                System.arraycopy(values, 0, vGyroscope, 0, vGyroscope.length);
                System.arraycopy(values, 0, mCurrentGyroscope, 0, values.length);
                mInitFlag = true;
            } else {
                vGyroscope[0] = alpha * mCurrentGyroscope[0] + (1.0f - alpha) * values[0];
                vGyroscope[1] = alpha * mCurrentGyroscope[1] + (1.0f - alpha) * values[1];
                vGyroscope[2] = alpha * mCurrentGyroscope[2] + (1.0f - alpha) * values[2];
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

            qGyroscopeDelta = new Quaternion(deltaVGyroscope[3], new Vector3D(deltaVGyroscope));
            mCurrentRotation = qGyroscopeDelta.multiply(mCurrentRotation);
            notifyHeadRotation(mCurrentRotation);
        }
        mLastEventTimestamp = event.timestamp;
    }

    @Override
    public void reset() {
        mCurrentRotation = new Quaternion(1, new Vector3D(0, 0, 0));
    }

}
