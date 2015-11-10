package org.deviceconnect.android.deviceplugin.theta.core.sensor;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DefaultHeadTracker extends AbstractHeadTracker implements SensorEventListener {

    private final SensorManager mSensorMgr;

    public DefaultHeadTracker(final SensorManager sensorMgr) {
        mSensorMgr = sensorMgr;
    }

    @Override
    public void start() {
        // TODO Register sensors.
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
        // TODO Detect the host device's attitude.
    }

}
