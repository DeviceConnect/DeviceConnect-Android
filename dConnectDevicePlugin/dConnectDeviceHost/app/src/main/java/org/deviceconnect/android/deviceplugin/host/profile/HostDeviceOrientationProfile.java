/*
 HostDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DeviceOrientation Profile.
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceOrientationProfile extends DeviceOrientationProfile implements SensorEventListener {

    /** 取得するべきセンサーの数. */
    private static final int NO_SENSOR = 3;

    /** SensorManager. */
    private SensorManager mSensorManager;

    /** ServiceID. */
    private String mServiceId;

    /** X軸方向の重力付き加速度. (単位: m/s^2). */
    private double mAccellX;

    /** Y軸方向の重力付き加速度. (単位: m/s^2). */
    private double mAccellY;

    /** Z軸方向の重力付き加速度. (単位: m/s^2). */
    private double mAccellZ;

    /** 加速度データが準備できているかどうかのフラグ */
    private AtomicBoolean mIsAccellReady = new AtomicBoolean(false);

    /** X軸方向の重力加速度成分. (単位: m/s^2). */
    private double mGravityX = 0;

    /** Y軸方向の重力加速度成分. (単位: m/s^2). */
    private double mGravityY = 0;

    /** Z軸方向の重力加速度成分. (単位: m/s^2). */
    private double mGravityZ = 0;

    /** 重力加速度データが準備できているかどうかのフラグ */
    private AtomicBoolean mIsGravityReady = new AtomicBoolean(false);

    /** X軸周りの角速度. (単位: degree/s). */
    private double mGyroX;

    /** Y軸周りの角速度. (単位: degree/s). */
    private double mGyroY;

    /** Z軸周りの角速度. (単位: degree/s). */
    private double mGyroZ;

    /** 角速度データが準備できているかどうかのフラグ */
    private AtomicBoolean mIsGyroReady = new AtomicBoolean(false);

    /** 前回の加速度の計測時間を保持する. */
    private long mAccelLastTime;

    /** センサー情報処理間隔設定用. */
    private long mSensorInterval;

    /** イベント送信間隔計測用. */
    private long mLastEventSendTime = 0;

    /** Device Orientationのデフォルト送信間隔を定義. */
    private static final long DEVICE_ORIENTATION_INTERVAL_TIME = 200;

    /** Device Orientationのキャッシュを残す時間を定義する. */
    private static final long DEVICE_ORIENTATION_CACHE_TIME = 100;

    private final DConnectApi mGetOnDeviceOrientationApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getDeviceOrientationEvent(response);
        }
    };

    private final DConnectApi mPutOnDeviceOrientationApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            try {
                String interval = request.getStringExtra(PARAM_INTERVAL);
                mSensorInterval = Long.parseLong(interval);
            } catch (NumberFormatException e) {
                mSensorInterval = DEVICE_ORIENTATION_INTERVAL_TIME;
            }

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                registerDeviceOrientationEvent(response, serviceId);
            } else {
                MessageUtils.setUnknownError(response, "Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDeviceOrientationApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                unregisterDeviceOrientationEvent(response);
            } else {
                MessageUtils.setUnknownError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    public HostDeviceOrientationProfile() {
        addApi(mGetOnDeviceOrientationApi);
        addApi(mPutOnDeviceOrientationApi);
        addApi(mDeleteOnDeviceOrientationApi);
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
        long t = System.currentTimeMillis() - mAccelLastTime;
        if (t > DEVICE_ORIENTATION_CACHE_TIME) {
            List<Sensor> sensors;
            final SensorEventListener l = new SensorEventListener() {
                @Override
                public void onSensorChanged(final SensorEvent event) {
                    processSensorData(event);

                    if (mIsAccellReady.get() || mIsGravityReady.get() || mIsGyroReady.get()) {
                        mAccelLastTime = System.currentTimeMillis();

                        Bundle orientation = createOrientation();
                        setResult(response, DConnectMessage.RESULT_OK);
                        setOrientation(response, orientation);
                        sendResponse(response);

                        if (isEmptyEventList()) {
                            mSensorManager.unregisterListener(this);
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
                    // No operation
                }
            };

            mSensorManager = getSensorManager();
            sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            int unsupported = 0;
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mSensorManager.registerListener(l, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                unsupported++;
            }

            sensors = mSensorManager
                    .getSensorList(Sensor.TYPE_GRAVITY);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mSensorManager.registerListener(l, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                unsupported++;
            }

            sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mSensorManager.registerListener(l, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                unsupported++;
            }

            if (unsupported == NO_SENSOR) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            invalidateLatestData();

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
     */
    private void registerDeviceOrientationEvent(final Intent response, final String serviceId) {

        mServiceId = serviceId;
        mSensorManager = getSensorManager();
        mAccelLastTime = System.currentTimeMillis();

        List<Sensor> sensors;
        int unsupported = 0;
        sensors = mSensorManager
                .getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor, (int)mSensorInterval * 1000);
        } else {
            unsupported++;
        }

        sensors = mSensorManager
                .getSensorList(Sensor.TYPE_GRAVITY);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor, (int)mSensorInterval * 1000);
        } else {
            unsupported++;
        }

        sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor, (int)mSensorInterval * 1000);
        } else {
            unsupported++;
        }

        if (unsupported == NO_SENSOR) {
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
        long interval = System.currentTimeMillis() - mLastEventSendTime;
        if (interval < 0) {
            interval = 0;
        }

        Bundle orientation = new Bundle();
        Bundle a1 = new Bundle();
        DeviceOrientationProfile.setX(a1, mAccellX - mGravityX);
        DeviceOrientationProfile.setY(a1, mAccellY - mGravityY);
        DeviceOrientationProfile.setZ(a1, mAccellZ - mGravityZ);

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

    private void processSensorData(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccellX = sensorEvent.values[0];
            mAccellY = sensorEvent.values[1];
            mAccellZ = sensorEvent.values[2];

            mIsAccellReady.compareAndSet(false, true);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mGravityX = sensorEvent.values[0];
            mGravityY = sensorEvent.values[1];
            mGravityZ = sensorEvent.values[2];

            mIsGravityReady.compareAndSet(false, true);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyroX = Math.toDegrees(sensorEvent.values[0]);
            mGyroY = Math.toDegrees(sensorEvent.values[1]);
            mGyroZ = Math.toDegrees(sensorEvent.values[2]);

            mIsGyroReady.compareAndSet(false, true);
        }
    }

    /**
     * キャッシュされたセンサーデータを無効扱いにし、最新センサーデータが全て揃うまでデータ収集を行わせる。
     */
    private void invalidateLatestData() {
        mIsAccellReady.compareAndSet(true, false);
        mIsGravityReady.compareAndSet(true, false);
        mIsGyroReady.compareAndSet(true, false);
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        processSensorData(sensorEvent);

        if (mIsAccellReady.get() || mIsGravityReady.get() || mIsGyroReady.get()) {
            Bundle orientation = createOrientation();
            mAccelLastTime = System.currentTimeMillis();

            if (isEmptyEventList()) {
                mSensorManager.unregisterListener(this);
                return;
            }

            long interval = System.currentTimeMillis() - mLastEventSendTime;
            if (interval > mSensorInterval) {
                List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                        DeviceOrientationProfile.PROFILE_NAME, null,
                        DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
                    sendEvent(intent, event.getAccessToken());
                }
                mLastEventSendTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // No operation
    }
}
