/*
 HostDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * DeviceOrientation Profile.
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceOrientationProfile extends DeviceOrientationProfile implements SensorEventListener {

    /** SensorManager. */
    private SensorManager mSensorManager;

    /** ServiceID. */
    private String mServiceId;

    /** 加速度 x. */
    private float mAccellX;

    /** 加速度 y. */
    private float mAccellY;

    /** 加速度 z. */
    private float mAccellZ;

    /** Gyro x. */
    private float mGyroX;

    /** Gyro y. */
    private float mGyroY;

    /** Gyro z. */
    private float mGyroZ;

    /** 前回の加速度の計測時間を保持する. */
    private long mAccelStartTime;

    /**
     * Device Orientationのキャッシュを残す時間を定義する.
     */
    private static final long DEVICE_ORIENTATION_CACHE_TIME = 100;

    @Override
    protected boolean onGetOnDeviceOrientation(final Intent request, final Intent response, final String serviceId) {
        boolean result = true;
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            result = getDeviceOrientationEvent(response);
        }
        return result;
    }

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                registerDeviceOrientationEvent(response, serviceId, sessionKey);
            } else {
                MessageUtils.setUnknownError(response, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                unregisterDeviceOrientationEvent(response);
            } else {
                MessageUtils.setUnknownError(response, "Can not unregister event.");
            }
        }
        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);
        return match.find();
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, "sessionKey is invalid.");
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * センサー管理クラスを取得する.
     * @return センサー管理クラス
     */
    private SensorManager getSensorManager() {
        return (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * イベント登録が空か確認する.
     * @return 空の場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEventList() {
        List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                DeviceOrientationProfile.PROFILE_NAME, null,
                DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        return events == null || events.size() == 0;
    }

    /**
     * Device Orientationのデータを取得する.
     * @param response データを格納するレスポンス
     * @return trueの場合には即座に値を返却する、falseの場合には返さない
     */
    private boolean getDeviceOrientationEvent(final Intent response) {
        long t = System.currentTimeMillis() - mAccelStartTime;
        if (t > DEVICE_ORIENTATION_CACHE_TIME) {
            List<Sensor> sensors;
            final SensorEventListener l = new SensorEventListener() {
                @Override
                public void onSensorChanged(final SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        mAccellX = event.values[0];
                        mAccellY = event.values[1];
                        mAccellZ = event.values[2];

                        Bundle orientation = createOrientation();
                        setResult(response, DConnectMessage.RESULT_OK);
                        setOrientation(response, orientation);
                        HostDeviceService service = (HostDeviceService) getContext();
                        service.sendResponse(response);

                        if (isEmptyEventList()) {
                            mSensorManager.unregisterListener(this);
                        }
                    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        mGyroX = event.values[0];
                        mGyroY = event.values[1];
                        mGyroZ = event.values[2];
                    }
                }
                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
                }
            }; 

            mSensorManager = getSensorManager();
            sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mSensorManager.registerListener(l, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
                mAccelStartTime = System.currentTimeMillis();
            } else {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mSensorManager.registerListener(l, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            return false;
        } else {
            Bundle orientation = createOrientation();
            setResult(response, DConnectMessage.RESULT_OK);
            setOrientation(response, orientation);
            return true;
        }
    }

    /**
     * Device Orientation Profile<br>
     * イベントの登録.
     * 
     * @param response
     *            レスポンス
     * @param serviceId
     *            サービスID
     * @param sessionKey
     *            セッションキー
     */
    private void registerDeviceOrientationEvent(final Intent response,
            final String serviceId, final String sessionKey) {

        mServiceId = serviceId;
        mSensorManager = getSensorManager();

        List<Sensor> sensors = mSensorManager
                .getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mAccelStartTime = System.currentTimeMillis();
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return;
        }

        sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return;
        }

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE,
                "Register OnDeviceOrientation event");
    }

    /**
     * Device Orientation Profile イベントの解除.
     * @param response レスポンス
     */
    private void unregisterDeviceOrientationEvent(final Intent response) {
        mSensorManager.unregisterListener(this);
        response.putExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE,
                "Unregister OnDeviceOrientation event");
    }

    /**
     * Orientationのデータを作成する.
     * @return Orientationのデータ
     */
    private Bundle createOrientation() {
        long interval = System.currentTimeMillis() - mAccelStartTime;

        Bundle orientation = new Bundle();
        Bundle a1 = new Bundle();
        DeviceOrientationProfile.setX(a1, 0.0);
        DeviceOrientationProfile.setY(a1, 0.0);
        DeviceOrientationProfile.setZ(a1, 0.0);

        Bundle a2 = new Bundle();
        DeviceOrientationProfile.setX(a2, mAccellX);
        DeviceOrientationProfile.setY(a2, mAccellY);
        DeviceOrientationProfile.setZ(a2, mAccellZ);

        Bundle r = new Bundle();
        DeviceOrientationProfile.setAlpha(r, mGyroX);
        DeviceOrientationProfile.setBeta(r, mGyroY);
        DeviceOrientationProfile.setGamma(r, mGyroZ);

        DeviceOrientationProfile.setAcceleration(orientation, a1);
        DeviceOrientationProfile.setAccelerationIncludingGravity(orientation, a2);
        DeviceOrientationProfile.setRotationRate(orientation, r);
        DeviceOrientationProfile.setInterval(orientation, interval);
        return orientation;
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            mAccellX = sensorEvent.values[0];
            mAccellY = sensorEvent.values[1];
            mAccellZ = sensorEvent.values[2];

            Bundle orientation = createOrientation();

            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                    DeviceOrientationProfile.PROFILE_NAME, null,
                    DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION,
                        orientation);
                getContext().sendBroadcast(intent);
            }

            mAccelStartTime = System.currentTimeMillis();
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyroX = sensorEvent.values[0];
            mGyroY = sensorEvent.values[1];
            mGyroZ = sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // No operation
    }
}
