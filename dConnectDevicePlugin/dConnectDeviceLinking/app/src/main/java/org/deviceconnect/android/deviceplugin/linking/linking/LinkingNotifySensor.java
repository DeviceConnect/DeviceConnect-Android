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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifySensor {
    private static final String TAG = "LinkingPlugIn";

    private final Map<LinkingDevice, List<LinkingDeviceManager.OnSensorListener>> mSensorMap = new HashMap<>();
    private final Map<LinkingDevice, List<LinkingDeviceManager.OnBatteryListener>> mBatteryMap = new HashMap<>();
    private final Map<LinkingDevice, List<LinkingDeviceManager.OnHumidityListener>> mHumidityMap = new HashMap<>();
    private final Map<LinkingDevice, List<LinkingDeviceManager.OnTemperatureListener>> mTemperatureMap = new HashMap<>();

    private NotifySensorData mNotifySensor;
    private LinkingDeviceManager mLinkingDeviceManager;
    private Context mContext;

    private final Map<String, Integer> mCountOfSensor = new HashMap<>();

    public LinkingNotifySensor(final Context context, final LinkingDeviceManager manager) {
        mContext = context;
        mLinkingDeviceManager = manager;
        startNotifySensor();
    }

    public synchronized void release() {
        mSensorMap.clear();
        mBatteryMap.clear();
        mHumidityMap.clear();
        mTemperatureMap.clear();
        mCountOfSensor.clear();

        if (mNotifySensor != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a sensor event.");
            }
            mNotifySensor.release();
            mNotifySensor = null;
        }
    }

    public synchronized void enableListenOrientation(final LinkingDevice device,
                                                     final LinkingDeviceManager.OnSensorListener listener) {
        if (!device.isSupportSensor()) {
            return;
        }

        List<LinkingDeviceManager.OnSensorListener> listeners = mSensorMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mSensorMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        if (listeners.size() > 1) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, device.getDisplayName() + ": orientation is already running.");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor:startOrientation: " + device.getDisplayName());
        }

        startSensor(device.getBdAddress(), getSupportSensorType(device), 200);
    }

    public synchronized void disableListenOrientation(final LinkingDevice device,
                                                      final LinkingDeviceManager.OnSensorListener listener) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor:disableListenOrientation: " + device.getDisplayName());
        }

        List<LinkingDeviceManager.OnSensorListener> listeners = mSensorMap.get(device);
        if (listeners != null) {
            if (listener == null) {
                mSensorMap.remove(device);
            } else {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mSensorMap.remove(device);
                }
            }
        }

        stopSensors(device);
    }

    public synchronized void enableListenBattery(final LinkingDevice device,
                                                 final LinkingDeviceManager.OnBatteryListener listener) {
        if (!device.isSupportBattery()) {
            return;
        }

        List<LinkingDeviceManager.OnBatteryListener> listeners = mBatteryMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mBatteryMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        if (listeners.size() > 1) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, device.getDisplayName() + ": battery is already running.");
            }
            return;
        }

        int[] type = { LinkingSensorData.SensorType.BATTERY.getValue() };
        startSensor(device.getBdAddress(), type, 1000);
    }

    public synchronized void disableListenBattery(final LinkingDevice device,
                                                  final LinkingDeviceManager.OnBatteryListener listener) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#disableListenBattery: " + device.getDisplayName());
        }

        List<LinkingDeviceManager.OnBatteryListener> listeners = mBatteryMap.get(device);
        if (listeners != null) {
            if (listener == null) {
                mBatteryMap.remove(device);
            } else {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mBatteryMap.remove(device);
                }
            }
        }

        stopSensors(device);
    }

    public synchronized void enableListenHumidity(final LinkingDevice device,
                                                  final LinkingDeviceManager.OnHumidityListener listener) {
        if (!device.isSupportHumidity()) {
            return;
        }

        List<LinkingDeviceManager.OnHumidityListener> listeners = mHumidityMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mHumidityMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        if (listeners.size() > 1) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, device.getDisplayName() + ": humidity is already running.");
            }
            return;
        }

        int[] type = { LinkingSensorData.SensorType.HUMIDITY.getValue() };
        startSensor(device.getBdAddress(), type, 1000);
    }

    public synchronized void disableListenHumidity(final LinkingDevice device,
                                                   final LinkingDeviceManager.OnHumidityListener listener) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#disableListenHumidity: " + device.getDisplayName());
        }

        List<LinkingDeviceManager.OnHumidityListener> listeners = mHumidityMap.get(device);
        if (listeners != null) {
            if (listener == null) {
                mHumidityMap.remove(device);
            } else {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mHumidityMap.remove(device);
                }
            }
        }

        stopSensors(device);
    }

    public synchronized void enableListenTemperature(final LinkingDevice device,
                                                     final LinkingDeviceManager.OnTemperatureListener listener) {
        if (!device.isSupportTemperature()) {
            return;
        }

        List<LinkingDeviceManager.OnTemperatureListener> listeners = mTemperatureMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mTemperatureMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        if (listeners.size() > 1) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, device.getDisplayName() + ": temperature is already running.");
            }
            return;
        }

        int[] type = { LinkingSensorData.SensorType.TEMPERATURE.getValue() };
        startSensor(device.getBdAddress(), type, 1000);
    }

    public synchronized void disableListenTemperature(final LinkingDevice device,
                                                      final LinkingDeviceManager.OnTemperatureListener listener) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#disableListenTemperature: " + device.getDisplayName());
        }

        List<LinkingDeviceManager.OnTemperatureListener> listeners = mTemperatureMap.get(device);
        if (listeners != null) {
            if (listener == null) {
                mTemperatureMap.remove(device);
            } else {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mTemperatureMap.remove(device);
                }
            }
        }

        stopSensors(device);
    }

    private boolean containsOrientation(final LinkingDevice device) {
        return mSensorMap.keySet().contains(device);
    }

    private boolean containsBattery(final LinkingDevice device) {
        return mBatteryMap.keySet().contains(device);
    }

    private synchronized boolean containsHumidity(final LinkingDevice device) {
        return mHumidityMap.keySet().contains(device);
    }

    private synchronized boolean containsTemperature(final LinkingDevice device) {
        return mTemperatureMap.keySet().contains(device);
    }

    private synchronized LinkingDevice findDeviceFromSensor(final String address) {
        for (LinkingDevice device : mSensorMap.keySet()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromBattery(final String address) {
        for (LinkingDevice device : mBatteryMap.keySet()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromHumidity(final String address) {
        for (LinkingDevice device : mHumidityMap.keySet()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized LinkingDevice findDeviceFromTemperature(final String address) {
        for (LinkingDevice device : mTemperatureMap.keySet()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private void startSensor(final String address, final int[] types, final int interval) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#startSensor: " + address);
        }

        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.START_SENSOR");
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.RECEIVER_NAME));
        intent.putExtra(mContext.getPackageName() + ".sda.extra.BD_ADDRESS", address);
        intent.putExtra(mContext.getPackageName() + ".sda.extra.SENSOR_INTERVAL", interval);
        intent.putExtra(mContext.getPackageName() + ".sda.extra.SENSOR_DURATION", -1);
        intent.putExtra(mContext.getPackageName() + ".sda.extra.X_THRESHOLD", 0.0F);
        intent.putExtra(mContext.getPackageName() + ".sda.extra.Y_THRESHOLD", 0.0F);
        intent.putExtra(mContext.getPackageName() + ".sda.extra.Z_THRESHOLD", 0.0F);

        for (int i = 0; i < types.length; i++) {
            start(intent, types[i]);
        }
        countUpSensor(address, types.length);
    }

    private void stopSensors(final LinkingDevice device) {
        if (device == null || containsBattery(device) || containsHumidity(device) ||
                containsOrientation(device) || containsTemperature(device)) {
            return;
        }

        int count = countDownSensor(device.getBdAddress());
        for (int i = 0; i < count; i++) {
            stopSensors(device.getBdAddress());
        }
    }

    private void stopSensors(final String address) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingNotifySensor#stopSensors: " + address);
        }

        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.STOP_SENSOR");
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.RECEIVER_NAME));
        intent.putExtra(mContext.getPackageName() + ".sda.extra.BD_ADDRESS", address);
        try {
            mLinkingDeviceManager.getContext().sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void countUpSensor(final String address, final int length) {
        Integer count = mCountOfSensor.get(address);
        if (count == null) {
            count = 0;
        }
        mCountOfSensor.put(address, count + length);
    }

    private int countDownSensor(final String address) {
        Integer count = mCountOfSensor.remove(address);
        if (count == null) {
            count = 1;
        }
        return count;
    }

    private int[] getSupportSensorType(final LinkingDevice device) {
        List<Integer> type = new ArrayList<>();
        if (device.isSupportGyro()) {
            type.add(LinkingSensorData.SensorType.GYRO.getValue());
        }
        if (device.isSupportAcceleration()) {
            type.add(LinkingSensorData.SensorType.ACCELERATION.getValue());
        }
        if (device.isSupportCompass()) {
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
        mNotifySensor = new NotifySensorData(mContext, new ControlSensorData.SensorRequestInterface() {
            @Override
            public void onStartSensorResult(final String bd, final int type, int resultCode) {
                if (BuildConfig.DEBUG) {
                    LinkingUtil.Result result = LinkingUtil.Result.valueOf(resultCode);
                    Log.e(TAG, "onStartSensorResult: " + bd + " " + type + " " + result);
                }
            }
        }, new ControlSensorData.SensorDataInterface() {
            private final LinkingSensorData mSensorData = new LinkingSensorData();
            @Override
            public synchronized void onSensorData(final String bd, final int type,
                                                  final float x, final float y, final float z,
                                                  final byte[] originalData, final long time) {
                mSensorData.setBdAddress(bd);
                mSensorData.setX(x);
                mSensorData.setY(y);
                mSensorData.setZ(z);
                mSensorData.setOriginalData(originalData);
                mSensorData.setType(LinkingSensorData.SensorType.valueOf(type));
                mSensorData.setTime(time);

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onSensorData: " + mSensorData);
                }

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
                LinkingDevice device = findDeviceFromBattery(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    int value = LinkingUtil.byteToShort(mSensorData.getOriginalData());
                    boolean lowBatteryFlag = (value & (1 << 11)) != 0;
                    float batteryLevel = (value & 0x07ff) / 10.0f;
                    notifyOnBattery(device, lowBatteryFlag, batteryLevel);
                }
            }

            private void onTemperature(final String bd) {
                LinkingDevice device = findDeviceFromTemperature(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    int value = LinkingUtil.byteToShort(mSensorData.getOriginalData());
                    float temperature = LinkingUtil.intToFloatIEEE754(value, 7, 4, true);
                    notifyOnTemperature(device, temperature);
                }
            }

            private void onHumidity(final String bd) {
                LinkingDevice device = findDeviceFromHumidity(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    int value = LinkingUtil.byteToShort(mSensorData.getOriginalData());
                    float humidity = LinkingUtil.intToFloatIEEE754(value, 8, 4, false);
                    notifyOnHumidity(device, humidity);
                }
            }

            private void onOrientationSensor(final String bd) {
                LinkingDevice device = findDeviceFromSensor(bd);
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
        for (LinkingDeviceManager.OnSensorListener listener : mSensorMap.get(device)) {
            listener.onChangeSensor(device, data);
        }
    }

    private synchronized void notifyOnBattery(final LinkingDevice device, final boolean lowBatteryFlag, final float batteryLevel) {
        for (LinkingDeviceManager.OnBatteryListener listener : mBatteryMap.get(device)) {
            listener.onBattery(device, lowBatteryFlag, batteryLevel);
        }
    }

    private synchronized void notifyOnHumidity(final LinkingDevice device, final float humidity) {
        for (LinkingDeviceManager.OnHumidityListener listener : mHumidityMap.get(device)) {
            listener.onHumidity(device, humidity);
        }
    }

    private synchronized void notifyOnTemperature(final LinkingDevice device, final float temperature) {
        for (LinkingDeviceManager.OnTemperatureListener listener : mTemperatureMap.get(device)) {
            listener.onTemperature(device, temperature);
        }
    }

    private void start(Intent request, int type) {
        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.START_SENSOR");
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.RECEIVER_NAME));
        intent.putExtras(request.getExtras());
        intent.putExtra(mContext.getPackageName() + ".sda.extra.SENSOR_TYPE", type);
        mContext.sendBroadcast(intent);
    }
}
