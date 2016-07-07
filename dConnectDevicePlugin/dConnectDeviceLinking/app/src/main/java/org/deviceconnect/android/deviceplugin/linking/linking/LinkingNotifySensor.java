/*
 LinkingNotifySensor.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.ControlSensorData;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

import java.util.ArrayList;
import java.util.List;

class LinkingNotifySensor {
    private static final String TAG = "LinkingPlugIn";

    private final List<LinkingDeviceManager.OnSensorListener> mOnSensorListeners = new ArrayList<>();
    private final List<LinkingDeviceManager.OnBatteryListener> mOnBatteryListeners = new ArrayList<>();
    private final List<LinkingDeviceManager.OnHumidityListener> mOnHumidityListeners = new ArrayList<>();
    private final List<LinkingDeviceManager.OnTemperatureListener> mOnTemperatureListeners = new ArrayList<>();

    private final List<LinkingDevice> mSensorDeviceHolders = new ArrayList<>();
    private final List<LinkingDevice> mBatteryDeviceHolders = new ArrayList<>();
    private final List<LinkingDevice> mHumidityDeviceHolders = new ArrayList<>();
    private final List<LinkingDevice> mTemperatureDeviceHolders = new ArrayList<>();

    private NotifySensorData mNotifySensor;
    private Context mContext;

    public LinkingNotifySensor(final Context context) {
        mContext = context;
        startNotifySensor();
    }

    public synchronized void release() {
        mOnSensorListeners.clear();
        mOnBatteryListeners.clear();
        mOnHumidityListeners.clear();
        mOnTemperatureListeners.clear();

        mSensorDeviceHolders.clear();
        mBatteryDeviceHolders.clear();
        mHumidityDeviceHolders.clear();
        mTemperatureDeviceHolders.clear();

        if (mNotifySensor != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a sensor event.");
            }
            mNotifySensor.release();
            mNotifySensor = null;
        }
    }

    public synchronized void startOrientation(final LinkingDevice device) {
        if (mSensorDeviceHolders.contains(device)) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor:startOrientation: " + device.getDisplayName());
        }

        mSensorDeviceHolders.add(device);
        startSensor(device.getBdAddress(), getSupportSensorType(device), 100);
    }

    public synchronized void stopOrientation(final LinkingDevice device) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor:stopOrientation: " + device.getDisplayName());
        }

        mSensorDeviceHolders.remove(device);
        stopSensors(device);
    }

    public synchronized boolean containsOrientation(final LinkingDevice device) {
        return mSensorDeviceHolders.contains(device);
    }

    public synchronized void startBattery(final LinkingDevice device) {
        if (mBatteryDeviceHolders.contains(device)) {
            return;
        }

        if (!device.isBattery()) {
            return;
        }

        int[] type = { LinkingSensorData.SensorType.BATTERY.getValue() };
        mBatteryDeviceHolders.add(device);
        startSensor(device.getBdAddress(), type, 100);
    }

    public synchronized void stopBattery(final LinkingDevice device) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#stopBattery: " + device.getDisplayName());
        }

        mBatteryDeviceHolders.remove(device);
        stopSensors(device);
    }

    public boolean containsBattery(final LinkingDevice device) {
        return mBatteryDeviceHolders.contains(device);
    }

    public synchronized void startHumidity(final LinkingDevice device) {
        if (mHumidityDeviceHolders.contains(device)) {
            return;
        }

        if (!device.isHumidity()) {
            return;
        }

        int[] type = { LinkingSensorData.SensorType.HUMIDITY.getValue() };
        mHumidityDeviceHolders.add(device);
        startSensor(device.getBdAddress(), type, 100);
    }

    public synchronized void stopHumidity(final LinkingDevice device) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#stopHumidity: " + device.getDisplayName());
        }

        mHumidityDeviceHolders.remove(device);
        stopSensors(device);
    }

    public synchronized boolean containsHumidity(final LinkingDevice device) {
        return mHumidityDeviceHolders.contains(device);
    }

    public synchronized void startTemperature(final LinkingDevice device) {
        if (mTemperatureDeviceHolders.contains(device)) {
            return;
        }

        if (!device.isHumidity()) {
            return;
        }

        int[] type = { LinkingSensorData.SensorType.TEMPERATURE.getValue() };
        mTemperatureDeviceHolders.add(device);
        startSensor(device.getBdAddress(), type, 100);
    }

    public synchronized void stopTemperature(final LinkingDevice device) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#stopTemperature: " + device.getDisplayName());
        }

        mTemperatureDeviceHolders.remove(device);
        stopSensors(device);
    }

    public synchronized boolean containsTemperature(final LinkingDevice device) {
        return mTemperatureDeviceHolders.contains(device);
    }

    public synchronized void addSensorListener(final LinkingDeviceManager.OnSensorListener listener) {
        mOnSensorListeners.add(listener);
    }

    public synchronized void removeSensorListener(final LinkingDeviceManager.OnSensorListener listener) {
        mOnSensorListeners.remove(listener);
    }

    public synchronized void addBatteryListener(final LinkingDeviceManager.OnBatteryListener listener) {
        mOnBatteryListeners.add(listener);
    }

    public synchronized void removeBatteryListener(final LinkingDeviceManager.OnBatteryListener listener) {
        mOnBatteryListeners.remove(listener);
    }

    public synchronized void addHumidityListener(final LinkingDeviceManager.OnHumidityListener listener) {
        mOnHumidityListeners.add(listener);
    }

    public synchronized void removeHumidityListener(final LinkingDeviceManager.OnHumidityListener listener) {
        mOnHumidityListeners.remove(listener);
    }

    public synchronized void addTemperatureListener(final LinkingDeviceManager.OnTemperatureListener listener) {
        mOnTemperatureListeners.add(listener);
    }

    public synchronized void removeTemperatureListener(final LinkingDeviceManager.OnTemperatureListener listener) {
        mOnTemperatureListeners.remove(listener);
    }

    private synchronized LinkingDevice findDeviceFromSensorHolders(final String address) {
        for (LinkingDevice device : mSensorDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromBatteryHolders(final String address) {
        for (LinkingDevice device : mBatteryDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromHumidityHolders(final String address) {
        for (LinkingDevice device : mHumidityDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromTemperatureHolders(final String address) {
        for (LinkingDevice device : mTemperatureDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private void startSensor(final String address, final int[] type, final int interval) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#startSensor: " + address);
        }

        Intent intent = new Intent(mContext, ConfirmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LinkingUtil.EXTRA_BD_ADDRESS, address);
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_INTERVAL, interval);
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_DURATION, -1);
        intent.putExtra(LinkingUtil.EXTRA_X_THRESHOLD, 0.0F);
        intent.putExtra(LinkingUtil.EXTRA_Y_THRESHOLD, 0.0F);
        intent.putExtra(LinkingUtil.EXTRA_Z_THRESHOLD, 0.0F);
        intent.putExtra(ConfirmActivity.EXTRA_REQUEST_SENSOR_TYPE, type);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    private void stopSensors(final LinkingDevice device) {
        if (containsBattery(device) || containsHumidity(device) ||
                containsOrientation(device) || containsTemperature(device)) {
            return;
        }
        stopSensors(device.getBdAddress());
    }

    private void stopSensors(final String address) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#stopSensors: " + address);
        }

        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.STOP_SENSOR");
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.RECEIVER_NAME));
        intent.putExtra(mContext.getPackageName() + ".sda.extra.BD_ADDRESS", address);
        try {
            mContext.sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private int[] getSupportSensorType(final LinkingDevice device) {
        List<Integer> type = new ArrayList<>();
        if (device.isGyro()) {
            type.add(LinkingSensorData.SensorType.GYRO.getValue());
        }
        if (device.isAcceleration()) {
            type.add(LinkingSensorData.SensorType.ACCELERATION.getValue());
        }
        if (device.isCompass()) {
            type.add(LinkingSensorData.SensorType.COMPASS.getValue());
        }
        int[] types = new int[type.size()];
        for (int i = 0; i < type.size(); i++) {
            types[i] = type.get(i);
        }
        return types;
    }

    private void startNotifySensor() {
        if (mNotifySensor != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifySensor is already running.");
            }
            return;
        }
        mNotifySensor = new NotifySensorData(mContext, new ControlSensorData.SensorDataInterface() {
            private final LinkingSensorData mSensorData = new LinkingSensorData();
            @Override
            public synchronized void onSensorData(final String bd, final int type,
                                                  final float x, final float y, final float z,
                                                  final byte[] originalData, final long time) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onSensorData:[" + bd + "] type:" + type + " time:" + time
                            + "( x: " + x + " y: " + y + " z: " + z + ")");
                }

                mSensorData.setBdAddress(bd);
                mSensorData.setX(x);
                mSensorData.setY(y);
                mSensorData.setZ(z);
                mSensorData.setOriginalData(originalData);
                mSensorData.setType(LinkingSensorData.SensorType.valueOf(type));
                mSensorData.setTime(time);

                switch (mSensorData.getType()) {
                    case GYRO:
                    case ACCELERATION:
                    case COMPASS:
                        onOrientationSensor(bd);
                        break;
                    case BATTERY:
                        onBatterySensor(bd);
                        break;
                    case TEMPERATURE:
                        onTemperature(bd);
                        break;
                    case HUMIDITY:
                        onHumidity(bd);
                        break;
                    default:
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Not support sensor type=" + mSensorData.getType());
                        }
                        break;
                }
            }

            @Override
            public void onStopSensor(final String bd, final int type, final int reason) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onStopSensor: type:[" + type + "] reason:[" + reason + "] bd: " + bd);
                }
            }

            private void onBatterySensor(final String bd) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Not support battery sensor.");
                }
                LinkingDevice device = findDeviceFromBatteryHolders(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    // TODO batteryの計算
                    notifyOnBattery(device, false, 0);
                }
            }

            private void onTemperature(final String bd) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Not support temperature sensor.");
                }
                LinkingDevice device = findDeviceFromTemperatureHolders(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    // TODO temperatureの計算
                    notifyOnTemperature(device, 0);
                }
            }

            private void onHumidity(final String bd) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Not support humidity sensor.");
                }

                LinkingDevice device = findDeviceFromHumidityHolders(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    // TODO humidityの計算
                    notifyOnHumidity(device, 0);
                }
            }

            private void onOrientationSensor(final String bd) {
                LinkingDevice device = findDeviceFromSensorHolders(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    notifyOnChangeSensor(device, mSensorData);
                }
            }
        });
    }

    private synchronized void notifyOnChangeSensor(final LinkingDevice device, final LinkingSensorData data) {
        for (LinkingDeviceManager.OnSensorListener listener : mOnSensorListeners) {
            listener.onChangeSensor(device, data);
        }
    }

    private synchronized void notifyOnBattery(final LinkingDevice device, final boolean lowBatteryFlag, final float batteryLevel) {
        for (LinkingDeviceManager.OnBatteryListener listener : mOnBatteryListeners) {
            listener.onBattery(device, lowBatteryFlag, batteryLevel);
        }
    }

    private synchronized void notifyOnHumidity(final LinkingDevice device, final float humidity) {
        for (LinkingDeviceManager.OnHumidityListener listener : mOnHumidityListeners) {
            listener.onHumidity(device, humidity);
        }
    }

    private synchronized void notifyOnTemperature(final LinkingDevice device, final float temperature) {
        for (LinkingDeviceManager.OnTemperatureListener listener : mOnTemperatureListeners) {
            listener.onTemperature(device, temperature);
        }
    }
}
