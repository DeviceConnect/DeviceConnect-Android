/*
 SpheroManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.Acceleration;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.LocatorData;
import com.orbotix.common.sensor.QuaternionSensor;
import com.orbotix.common.sensor.ThreeAxisSensor;
import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.BackLED;
import com.orbotix.macro.cmd.Delay;
import com.orbotix.macro.cmd.RGB;

import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroLightProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Spheroの操作機能を提供するクラス.
 * @author NTT DOCOMO, INC.
 */
public final class SpheroManager implements DeviceInfo.DeviceSensorListener, DeviceInfo.DeviceCollisionListener {
    /** TAG. */
    private static final String TAG = SpheroManager.class.getSimpleName();

    /**
     * シングルトンなManagerのインスタンス.
     */
    public static final SpheroManager INSTANCE = new SpheroManager();

     /**
     * 切断のリトライ回数.
     */
    private static final int DISCONNECTION_RETRY_NUM = 50;

    /**
     * 切断のリトライ遅延.
     */
    private static final int DISCONNECTION_RETRY_DELAY = 1000;
    /**
     * Spheroを見失った際のリトライ回数.
     */
    private static final int SEARCH_RETRY_NUM = 5;

    /**
     * 1G = {@value} .
     */
    private static final double G = 9.81;
    /**
     * ScheduledExecutorServiceインスタンス.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Spheroが見つからなくなったことを検知するためのタイマー.
     */
    private ScheduledFuture<?> mScanTimerFuture;

    /**
     * 初回実行時間.
     */
    private static final long SCAN_FIRST_WAIT_PERIOD = 1000;

    /**
     * 次回実行時.
     */
    private static final long SCAN_WAIT_PERIOD = 5 * 1000;

    /**
     * 検知したデバイス一覧.
     */
    private ConcurrentHashMap<String, DeviceInfo> mDevices;


    /**
     * 検知されたデバイスの一覧. まだ未接続で、検知されただけの状態の一覧.
     */
    private List<Robot> mFoundDevices = Collections.synchronizedList(new ArrayList<Robot>());

    /**
     * 見つからなくなったデバイスを管理するオブジェクト.
     */
    private Map<String, Integer> mCounting = new HashMap<String, Integer>();

    /**
     * デバイス検知リスナー.
     */
    private DeviceDiscoveryListener mDiscoveryListener;

    /**
     * サービス.
     */
    private SpheroDeviceService mService;

    /**
     * 一時的にDeviceInfoをキャッシュする変数.
     */
    private DeviceInfo mCacheDeviceInfo;

    /**
     * 一時的にDeviceSensorsDataをキャッシュする変数.
     */
    private DeviceSensorAsyncMessage mCacheDeviceSensorsData;
    /**
     * Discovery Listener.
     */
    private DiscoveryListenerImpl mDiscoveryListenerImpl;
    /**
     * 一時的にインターバルをキャッシュする変数.
     */
    private long mCacheInterval;

    /**
     * デバイス検知の通知を受けるリスナー.
     */
    public interface DeviceDiscoveryListener {

        /**
         * 見つかったデバイスを通知します.
         *
         * @param sphero 見つかったデバイス
         */
        void onDeviceFound(ConvenienceRobot sphero);

        /**
         * 消失したデバイスの通知を受けるリスナー.
         *
         * @param sphero 消失したデバイス
         */
        void onDeviceLost(ConvenienceRobot sphero);

        /**
         * すべてのデバイスの消失を通知します.
         */
        void onDeviceLostAll();
    }

    /**
     * Spheroを操作するためのオブジェクト
     */
    private DualStackDiscoveryAgent discoveryAgent;

    /**
     * タイムアウト.
     */
    private int mConnectingTimeoutCount;
    private boolean mScanning = false;
    /**
     * 接続中のフラグリスト.
     */
    private ConcurrentMap<String, Boolean> mConnectingFlags;
    /**
     * SpheroManagerを生成する.
     */
    private SpheroManager() {
        mDevices = new ConcurrentHashMap<String, DeviceInfo>();
        discoveryAgent = DualStackDiscoveryAgent.getInstance();
        discoveryAgent.setMaxConnectedRobots(4);
        mDiscoveryListenerImpl = new DiscoveryListenerImpl();
        discoveryAgent.addRobotStateListener(mDiscoveryListenerImpl);
        mConnectingTimeoutCount = 0;
        mConnectingFlags = new ConcurrentHashMap<>();
    }

    /**
     * 検知を開始する.
     *
     * @param context コンテキストオブジェクト.
     */
    public synchronized void startDiscovery(final Context context) {
        try {
            if (!discoveryAgent.isDiscovering()) {
                discoveryAgent.startDiscovery(context);
            }
        } catch (DiscoveryException e) {
            e.printStackTrace();
        }
    }

    /**
     * デバイス検知のリスナーを設定する.
     *
     * @param listener リスナー
     */
    public synchronized void setDiscoveryListener(final DeviceDiscoveryListener listener) {
        mDiscoveryListener = listener;
    }

    /**
     * 検知を終了する.
     */
    public synchronized void stopDiscovery() {
        if (discoveryAgent.isDiscovering()) {
            discoveryAgent.stopDiscovery();
        }

    }

    /**
     * Spheroの操作を全てシャットダウンさせる.
     */
    public synchronized void shutdown() {
        stopDiscovery();
        discoveryAgent.removeRobotStateListener(mDiscoveryListenerImpl);
        mDiscoveryListenerImpl = null;
        discoveryAgent.disconnectAll();
        mService = null;
    }

    /**
     * 検知したデバイスの一覧を取得する.
     *
     * @return デバイス一覧
     */
    public synchronized List<Robot> getFoundDevices() {
        return mFoundDevices;
    }

    /**
     * 接続済みのデバイス一覧を取得する.
     *
     * @return 接続済みのデバイス一覧
     */
    public synchronized Collection<DeviceInfo> getConnectedDevices() {
        return mDevices.values();
    }

    /**
     * 指定されたサービスIDを持つデバイスを取得する.
     *
     * @param serviceId サービスID
     * @return デバイス。無い場合はnullを返す。
     */
    public DeviceInfo getDevice(final String serviceId) {
        return mDevices.get(serviceId);
    }

    /**
     * 未接続の端末一覧から一致するものを取得する.
     *
     * @param serviceId サービスID
     * @return デバイス。無い場合はnull。
     */
    public synchronized Robot getNotConnectedDevice(final String serviceId) {
        if (mFoundDevices == null) {
            return null;
        }

        for (Robot s : mFoundDevices) {
            if (s.getIdentifier().equals(serviceId)) {
                return s;
            }
        }

        return null;
    }


    /**
     * 未接続の端末一覧から一致するものを削除する.
     *
     * @param serviceId サービスID
     * @return デバイス。無い場合はnull。
     */
    public synchronized Robot removeNotConnectedDevice(final String serviceId) {
        if (mFoundDevices == null) {
            return null;
        }

        for (int i = 0; i < mFoundDevices.size(); i++) {
            if (mFoundDevices.get(i).getIdentifier().equals(serviceId)) {
                mCounting.remove(serviceId);
                mDevices.remove(serviceId);
                return mFoundDevices.remove(i);
            }
        }

        return null;
    }

    /**
     * 指定されたIDのSpheroを接続解除する.
     *
     * @param id SpheroのUUID
     */
    public void disconnect(final String id) {
        if (id == null) {
            return;
        }
        DeviceInfo removed = mDevices.remove(id);
        if (removed != null) {
            ConvenienceRobot sphero = removed.getDevice();
            sphero.disconnect();
        }
    }

    /**
     * 指定されたIDを持つSpheroに接続する.
     *
     * @param id SpheroのUUID
     * @return 成功の場合 true、失敗ならfalseを返す。
     */
    public boolean connect(final String id) {
        Robot connected = null;
        synchronized (this) {
            if (mFoundDevices == null) {
                return false;
            }

            for (Robot s : mFoundDevices) {
                if (s.getIdentifier().equals(id)) {
                    if (s.isOnline()) {
                        return true;
                    }
                    connected = s;
                    break;
                }
            }
        }
        ConvenienceRobot cRobot = null;

        if (connected != null) {
            mConnectingFlags.put(connected.getIdentifier(), true);
            mConnectingTimeoutCount = 0;
            discoveryAgent.connect(connected);
            do {
                try {
                    Thread.sleep(DISCONNECTION_RETRY_DELAY);
                } catch (InterruptedException e) {
                    continue;
                }
                mConnectingTimeoutCount++;
                DeviceInfo info = mDevices.get(connected.getIdentifier());
                if (info != null) {
                    cRobot = info.getDevice();

                    if (cRobot.isConnected()) {
                        break;
                    }
                }
            } while (mConnectingTimeoutCount < DISCONNECTION_RETRY_NUM);

        }
        return (cRobot != null) && cRobot.isConnected();

    }

    /**
     * 指定されたデバイスのセンサーを1回だけ監視する.
     *
     * @param device   デバイス
     * @param listener 監視結果を通知するリスナー
     */
    public void startSensor(final DeviceInfo device, final DeviceInfo.DeviceSensorListener listener) {
        synchronized (device) {
            if (!device.isSensorStarted()) {
                device.startSensor((info, data, interval) -> {
                    if (listener != null) {
                        listener.sensorUpdated(info, data, interval);
                    }
                    if (!hasSensorListener(device.getDevice().getRobot().getIdentifier())) {
                        stopSensor(device);
                    }
                });
            } else {
                if (listener != null) {
                    listener.sensorUpdated(mCacheDeviceInfo,
                            mCacheDeviceSensorsData, mCacheInterval);
                }
            }
        }
    }

    /**
     * 指定されたデバイスのセンサー監視を開始する.
     *
     * @param device デバイス
     */
    public void startSensor(final DeviceInfo device) {
        synchronized (device) {
            if (!device.isSensorStarted()) {
                device.startSensor(this);
            }
        }
    }

    /**
     * 指定されたデバイスのセンサー監視を停止する.
     *
     * @param device デバイス
     */
    public void stopSensor(final DeviceInfo device) {
        synchronized (device) {
            if (device.isSensorStarted()) {
                device.stopSensor();
            }
        }
    }

    /**
     * 指定されたデバイスの衝突監視を開始する.
     *
     * @param device デバイス
     */
    public void startCollision(final DeviceInfo device) {
        synchronized (device) {
            if (!device.isCollisionStarted()) {
                device.startCollistion(this);
            }
        }
    }

    /**
     * 指定されたデバイスの衝突監視を開始する.
     *
     * @param device   デバイス
     * @param listener リスナー
     */
    public void startCollision(final DeviceInfo device, final DeviceInfo.DeviceCollisionListener listener) {
        synchronized (device) {
            if (!device.isCollisionStarted()) {
                device.startCollistion((info, data) -> {
                    if (listener != null) {
                        listener.collisionDetected(info, data);
                    }
                    if (!hasCollisionListener(device.getDevice().getRobot().getIdentifier())) {
                        stopCollision(device);
                    }
                });
            }
        }
    }

    /**
     * 指定されたデバイスの衝突監視を停止する.
     *
     * @param device デバイス
     */
    public void stopCollision(final DeviceInfo device) {
        synchronized (device) {
            if (device.isCollisionStarted()) {
                device.stopCollision();
            }
        }
    }

    /**
     * サービスを設定する.
     *
     * @param service サービス
     */
    public void setService(final SpheroDeviceService service) {
        mService = service;
    }

    /**
     * センサー系のイベントを持っているかチェックする.
     *
     * @param info デバイス
     * @return 持っているならtrue、その他はfalseを返す。
     */
    public boolean hasSensorEvent(final DeviceInfo info) {

        List<Event> eventQua = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(),
                SpheroProfile.PROFILE_NAME, SpheroProfile.INTER_QUATERNION, SpheroProfile.ATTR_ON_QUATERNION);

        List<Event> eventOri = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(),
                DeviceOrientationProfile.PROFILE_NAME, null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        List<Event> eventLoc = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(),
                SpheroProfile.PROFILE_NAME, SpheroProfile.INTER_LOCATOR, SpheroProfile.ATTR_ON_LOCATOR);

        return (eventOri.size() != 0) || (eventQua.size() != 0) || (eventLoc.size() != 0);
    }

    /**
     * バックライトを点滅させる.
     *
     * @param info      デバイス情報
     * @param intensity 明るさ
     * @param pattern   パターン
     */
    public static void flashBackLight(final DeviceInfo info, final int intensity, final long[] pattern) {
        MacroObject m = new MacroObject();

        for (int i = 0; i < pattern.length; i++) {
            if (i % 2 == 0) {
                m.addCommand(new BackLED(intensity, 0));
            } else {
                m.addCommand(new BackLED(0, 0));
            }
            m.addCommand(new Delay((int) pattern[i]));
        }
        int oriIntensity = (int) (info.getBackBrightness() * SpheroLightProfile.MAX_BRIGHTNESS);
        m.addCommand(new BackLED(oriIntensity, 0));
        info.getDevice().playMacro(m);
    }

    /**
     * フロントライトを点滅させる.
     *
     * @param info    デバイス情報
     * @param colors  色
     * @param pattern パターン
     */
    public static void flashFrontLight(final DeviceInfo info, final int[] colors, final long[] pattern) {
        MacroObject m = new MacroObject();
        for (int i = 0; i < pattern.length; i++) {
            if (i % 2 == 0) {
                m.addCommand(new RGB(colors[0], colors[1], colors[2], 0));
            } else {
                m.addCommand(new RGB(0, 0, 0, 0));
            }
            m.addCommand(new Delay((int) pattern[i]));
        }
        info.getDevice().playMacro(m);
    }

    /**
     * 指定されたデータからOrientationデータを作成する.
     *
     * @param data     データ
     * @param interval インターバル
     * @return Orientationデータ
     */
    public static Bundle createOrientation(final DeviceSensorAsyncMessage data, final long interval) {
        Acceleration accData = data.getAsyncData().get(0).getAccelerometerData().getFilteredAcceleration();
        Bundle accelerationIncludingGravity = new Bundle();
        // Spheroでは単位がG(1G=9.81m/s^2)で正規化しているので、Device Connectの単位(m/s^2)に変換する。
        DeviceOrientationProfile.setX(accelerationIncludingGravity, accData.x * G);
        DeviceOrientationProfile.setY(accelerationIncludingGravity, accData.y * G);
        DeviceOrientationProfile.setZ(accelerationIncludingGravity, accData.z * G);

        GyroData gyroData = data.getAsyncData().get(0).getGyroData();
        ThreeAxisSensor threeAxisSensor = gyroData.getRotationRateFiltered();
        Bundle rotationRate = new Bundle();
        DeviceOrientationProfile.setAlpha(rotationRate, 0.1d * threeAxisSensor.x);
        DeviceOrientationProfile.setBeta(rotationRate, 0.1d * threeAxisSensor.y);
        DeviceOrientationProfile.setGamma(rotationRate, 0.1d * threeAxisSensor.z);

        Bundle orientation = new Bundle();
        DeviceOrientationProfile.setAccelerationIncludingGravity(orientation, accelerationIncludingGravity);
        DeviceOrientationProfile.setRotationRate(orientation, rotationRate);
        DeviceOrientationProfile.setInterval(orientation, interval);
        return orientation;
    }

    /**
     * 指定されたデータからQuaternionデータを作成する.
     *
     * @param data     データ
     * @param interval インターバル
     * @return Quaternionデータ
     */
    public static Bundle createQuaternion(final DeviceSensorAsyncMessage data, final long interval) {
        QuaternionSensor quat = data.getAsyncData().get(0).getQuaternion();
        Bundle quaternion = new Bundle();
        quaternion.putDouble(SpheroProfile.PARAM_Q0, quat.q0);
        quaternion.putDouble(SpheroProfile.PARAM_Q1, quat.q1);
        quaternion.putDouble(SpheroProfile.PARAM_Q2, quat.q2);
        quaternion.putDouble(SpheroProfile.PARAM_Q3, quat.q3);
        quaternion.putLong(SpheroProfile.PARAM_INTERVAL, interval);
        return quaternion;
    }

    /**
     * 指定されたデータからLocatorデータを作成する.
     *
     * @param data データ
     * @return Locatorデータ
     */
    public static Bundle createLocator(final DeviceSensorAsyncMessage data) {
        LocatorData loc = data.getAsyncData().get(0).getLocatorData();
        Bundle locator = new Bundle();
        locator.putFloat(SpheroProfile.PARAM_POSITION_X, loc.getPositionX());
        locator.putFloat(SpheroProfile.PARAM_POSITION_Y, loc.getPositionY());
        locator.putFloat(SpheroProfile.PARAM_VELOCITY_X, loc.getVelocityX());
        locator.putFloat(SpheroProfile.PARAM_VELOCITY_Y, loc.getVelocityY());
        return locator;
    }

    /**
     * 指定されたデータからCollisionデータを作成する.
     *
     * @param data データ
     * @return Collisionデータ
     */
    public static Bundle createCollision(final CollisionDetectedAsyncData data) {
        Bundle collision = new Bundle();

        Acceleration impactAccelerationData = data.getImpactAcceleration();
        Bundle impactAcceleration = new Bundle();
        impactAcceleration.putDouble(SpheroProfile.PARAM_X, impactAccelerationData.x);
        impactAcceleration.putDouble(SpheroProfile.PARAM_Y, impactAccelerationData.y);
        impactAcceleration.putDouble(SpheroProfile.PARAM_Z, impactAccelerationData.z);

        Bundle impactAxis = new Bundle();
        impactAxis.putBoolean(SpheroProfile.PARAM_X, data.hasImpactXAxis());
        impactAxis.putBoolean(SpheroProfile.PARAM_Y, data.hasImpactYAxis());

        CollisionDetectedAsyncData.CollisionPower power = data.getImpactPower();
        Bundle impactPower = new Bundle();
        impactPower.putShort(SpheroProfile.PARAM_X, power.x);
        impactPower.putShort(SpheroProfile.PARAM_Y, power.y);

        collision.putBundle(SpheroProfile.PARAM_IMPACT_ACCELERATION, impactAcceleration);
        collision.putBundle(SpheroProfile.PARAM_IMPACT_AXIS, impactAxis);
        collision.putBundle(SpheroProfile.PARAM_IMPACT_POWER, impactPower);
        collision.putFloat(SpheroProfile.PARAM_IMPACT_SPEED, data.getImpactSpeed());
        collision.putLong(SpheroProfile.PARAM_IMPACT_TIMESTAMP, data.getTimeStamp().getTime());
        collision.putString(SpheroProfile.PARAM_IMPACT_TIMESTAMPSTRING, RFC3339DateUtils.toString(data.getTimeStamp().getTime()));
        return collision;
    }

    /**
     * Spheroが接続された時の処理.
     *
     * @param sphero 接続されたSphero
     */
    private void onConnected(final ConvenienceRobot sphero) {
        DeviceInfo info = new DeviceInfo();
        info.setDevice(sphero);
        info.setBackBrightness(1.f);
        sphero.enableStabilization(true);
        if (!mDevices.contains(sphero.getRobot().getIdentifier())) {
            mDevices.put(sphero.getRobot().getIdentifier(), info);

        }
    }

    /**
     * センサーのイベントが登録されているか確認する.
     *
     * @param serviceId サービスID
     * @return 登録されている場合はtrue、それ以外はfalse
     */
    private boolean hasSensorListener(final String serviceId) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                DeviceOrientationProfile.PROFILE_NAME, null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        if (events != null && events.size() > 0) {
            return true;
        }

        events = EventManager.INSTANCE.getEventList(serviceId, SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_QUATERNION, SpheroProfile.ATTR_ON_QUATERNION);
        if (events != null && events.size() > 0) {
            return true;
        }

        events = EventManager.INSTANCE.getEventList(serviceId, SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_LOCATOR, SpheroProfile.ATTR_ON_LOCATOR);
        return (events != null && events.size() > 0);
    }

    /**
     * 衝突のイベントが登録されているか確認する.
     *
     * @param serviceId サービスID
     * @return 登録されている場合はtrue、それ以外はfalse
     */
    private boolean hasCollisionListener(final String serviceId) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_COLLISION,
                SpheroProfile.ATTR_ON_COLLISION);
        return events != null && events.size() > 0;
    }



    /**
     * 検知リスナー.
     */
    private class DiscoveryListenerImpl implements RobotChangedStateListener {
        @Override
        public void handleRobotChangedState(final Robot robot, final RobotChangedStateNotificationType robotChangedStateNotificationType) {
            ConvenienceRobot cRobot = new ConvenienceRobot(robot);
            switch (robotChangedStateNotificationType) {
                case Online:
                    SpheroManager.this.onConnected(cRobot);
                    if (mDiscoveryListener != null) {
                        mDiscoveryListener.onDeviceFound(cRobot);
                    }
                    // 接続されているデバイスは再接続対象から外す
                    mCounting.remove(robot.getIdentifier());
                    if (mCounting.size() == 0) {
                        scanSphero(false);
                    }
                    break;
                case Connecting:
                    if (getNotConnectedDevice(robot.getIdentifier()) == null) {
                        mFoundDevices.add(robot);
                    }
                    break;
                case Connected:
                    break;
                case Offline:
                case Disconnected:
                case FailedConnect:
                    mCounting.put(robot.getIdentifier(), 1);
                    if (mCounting.size() > 0) {
                        scanSphero(true);
                    }
                    if (mDiscoveryListener != null) {
                        mDiscoveryListener.onDeviceLost(cRobot);
                    }
                    break;
                default:
            }
        }

    }

    @Override
    public void sensorUpdated(final DeviceInfo info, final DeviceSensorAsyncMessage data, final long interval) {

        if (mService == null) {
            return;
        }

        mCacheDeviceInfo = info;
        mCacheDeviceSensorsData = data;
        mCacheInterval = interval;
        List<Event> events = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(),
                DeviceOrientationProfile.PROFILE_NAME, null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        if (events.size() != 0) {
            Bundle orientation = createOrientation(data, interval);
            synchronized (events) {
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    DeviceOrientationProfile.setOrientation(event, orientation);
                    mService.sendEvent(event, e.getAccessToken());
                }
            }
        }

        events = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(), SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_QUATERNION, SpheroProfile.ATTR_ON_QUATERNION);

        if (events.size() != 0) {
            Bundle quaternion = createQuaternion(data, interval);
            synchronized (events) {
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(SpheroProfile.PARAM_QUATERNION, quaternion);
                    mService.sendEvent(event, e.getAccessToken());
                }
            }
        }

        events = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(), SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_LOCATOR, SpheroProfile.ATTR_ON_LOCATOR);

        if (events.size() != 0) {
            Bundle locator = createLocator(data);
            synchronized (events) {
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(SpheroProfile.PARAM_LOCATOR, locator);
                    mService.sendEvent(event, e.getAccessToken());
                }
            }
        }
    }

    @Override
    public void collisionDetected(final DeviceInfo info, final CollisionDetectedAsyncData data) {
        if (mService == null) {
            return;
        }

        List<Event> events = EventManager.INSTANCE.getEventList(info.getDevice().getRobot().getIdentifier(),
                SpheroProfile.PROFILE_NAME,
                SpheroProfile.INTER_COLLISION,
                SpheroProfile.ATTR_ON_COLLISION);

        if (events.size() != 0) {
            Bundle collision = createCollision(data);
            synchronized (events) {
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(SpheroProfile.PARAM_COLLISION, collision);
                    mService.sendEvent(event, e.getAccessToken());
                }
            }
        }
    }

    /**
     * 再接続用タイマー.
     * @param enable true:タイマーのスタート false:タイマーのストップ
     */
    private synchronized void scanSphero(final boolean enable) {
        if (enable) {
            if (mScanning || mScanTimerFuture != null) {
                // scan have already started.
                return;
            }
            mScanning = true;
            mScanTimerFuture = mExecutor.scheduleAtFixedRate(() -> {
                for (String serviceId : mCounting.keySet()) {
                    Integer count = mCounting.get(serviceId);
                    count++;
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "retryConnecting:" + count);
                    }
                    mCounting.put(serviceId, count);
                    if (count >= SEARCH_RETRY_NUM) {
                        Robot info = getNotConnectedDevice(serviceId);
                        if (mDiscoveryListener != null
                                && info != null
                                && !info.isOnline()) {
                            mDiscoveryListener.onDeviceFound(new ConvenienceRobot(info));
                        }
                        mCounting.put(serviceId, 0);
                        if (mCounting.size() == 0) {
                            mScanning = false;
                            cancelScanTimer();
                        }
                    }
                }
            }, SCAN_FIRST_WAIT_PERIOD, SCAN_WAIT_PERIOD, TimeUnit.MILLISECONDS);
        } else {
            mScanning = false;
            cancelScanTimer();
        }
    }

    /**
     * タイマーを止める.
     */
    private synchronized void cancelScanTimer() {
        if (mScanTimerFuture != null) {
            mScanTimerFuture.cancel(true);
            mScanTimerFuture = null;
        }
    }
}
