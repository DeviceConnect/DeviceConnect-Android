/*
 SpheroManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import orbotix.macro.BackLED;
import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.RGB;
import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.sensor.Acceleration;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.robot.sensor.GyroData;
import orbotix.robot.sensor.LocatorData;
import orbotix.robot.sensor.QuaternionSensor;
import orbotix.robot.sensor.ThreeAxisSensor;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.DiscoveryListener;
import orbotix.sphero.Sphero;

import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo.DeviceCollisionListener;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo.DeviceSensorListener;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroLightProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.DeviceOrientationProfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Spheroの操作機能を提供するクラス.
 * @author NTT DOCOMO, INC.
 */
public final class SpheroManager implements DeviceSensorListener, DeviceCollisionListener {

    /**
     * シングルトンなManagerのインスタンス.
     */
    public static final SpheroManager INSTANCE = new SpheroManager();

    /**
     * 接続のタイムアウト.
     */
    private static final int CONNECTION_TIMEOUT = 30000;
    
    /**
     * 切断のリトライ回数.
     */
    private static final int DISCONNECTION_RETRY_NUM = 10;
    
    /**
     * 切断のリトライ遅延.
     */
    private static final int DISCONNECTION_RETRY_DELAY = 1000;

    /**
     * 1G = {@value} .
     */
    private static final double G = 9.81;

    /**
     * 検知したデバイス一覧.
     */
    private ConcurrentHashMap<String, DeviceInfo> mDevices;

    /**
     * 検知中フラグ.
     */
    private boolean mIsDiscovering;

    /**
     * 検知されたデバイスの一覧. まだ未接続で、検知されただけの状態の一覧.
     */
    private List<Sphero> mFoundDevices;

    /**
     * デバイス検知リスナー.
     */
    private DeviceDiscoveryListener mDiscoveryListener;

    /**
     * Sphereの操作クラス.
     */
    private RobotProvider mRobotProvider;

    /**
     * 接続のロック.
     */
    private Object mConnLock;

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
    private DeviceSensorsData mCacheDeviceSensorsData;

    /**
     * 一時的にインターバルをキャッシュする変数.
     */
    private long mCacheInterval;

    /**
     * SpheroManagerを生成する.
     */
    private SpheroManager() {
        mDevices = new ConcurrentHashMap<String, DeviceInfo>();
        mRobotProvider = RobotProvider.getDefaultProvider();
        mRobotProvider.addConnectionListener(new ConnectionListenerImpl());
        mRobotProvider.addDiscoveryListener(new DiscoveryListenerImpl());
        mConnLock = new Object();
    }

    /**
     * 検知を開始する.
     * 
     * @param context コンテキストオブジェクト.
     */
    public synchronized void startDiscovery(final Context context) {

        if (mIsDiscovering) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d("", "start discovery");
        }
        
        mIsDiscovering = mRobotProvider.startDiscovery(context);
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

        if (!mIsDiscovering) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d("", "stop discovery");
        }

        mRobotProvider.endDiscovery();
        mIsDiscovering = false;
        if (mFoundDevices != null) {
            mFoundDevices.clear();
        }
    }

    /**
     * Spheroの操作を全てシャットダウンさせる.
     */
    public synchronized void shutdown() {
        stopDiscovery();
        mRobotProvider.removeAllControls();
        mRobotProvider.removeConnectionListeners();
        mRobotProvider.removeDiscoveryListeners();
        mRobotProvider.disconnectControlledRobots();
        mRobotProvider.shutdown();
        mService = null;
    }

    /**
     * 検知したデバイスの一覧を取得する.
     * 
     * @return デバイス一覧
     */
    public synchronized List<Sphero> getFoundDevices() {
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
    public synchronized Sphero getNotConnectedDevice(final String serviceId) {
        if (mFoundDevices == null) {
            return null;
        }
        
        for (Sphero s : mFoundDevices) {
            if (s.getUniqueId().equals(serviceId)) {
                return s;
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
            final Sphero sphero = removed.getDevice();
            for (int i = 0; i < DISCONNECTION_RETRY_NUM; i++) {
                if (!sphero.isConnected()) {
                    break;
                }
                sphero.disconnect();
                try {
                    Thread.sleep(DISCONNECTION_RETRY_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 指定されたIDを持つSpheroに接続する.
     * 
     * @param id SpheroのUUID
     * @return 成功の場合 true、失敗ならfalseを返す。
     */
    public boolean connect(final String id) {

        Sphero connected = null;

        synchronized (this) {
            if (mFoundDevices == null) {
                return false;
            }

            for (Sphero s : mFoundDevices) {
                if (s.getUniqueId().equals(id)) {
                    if (s.isConnected()) {
                        return true;
                    }
                    connected = s;
                    break;
                }
            }
        }
        
        if (connected != null) {
            synchronized (mConnLock) {
                mRobotProvider.connect(connected);
                try {
                    mConnLock.wait(CONNECTION_TIMEOUT);
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    connected = null;
                }
            }
        }

        if (connected != null) {
            return connected.isConnected();
        }

        return false;
    }

    /**
     * 指定されたデバイスのセンサーを1回だけ監視する.
     * @param device デバイス
     * @param listener 監視結果を通知するリスナー
     */
    public void startSensor(final DeviceInfo device, final DeviceSensorListener listener) {
        synchronized (device) {
            if (!device.isSensorStarted()) {
                device.startSensor(new DeviceSensorListener() {
                    @Override
                    public void sensorUpdated(final DeviceInfo info,
                            final DeviceSensorsData data, final long interval) {
                        if (listener != null) {
                            listener.sensorUpdated(info, data, interval);
                        }
                        if (!hasSensorListener(device.getDevice().getUniqueId())) {
                            stopSensor(device);
                        }
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
     * @param device デバイス
     * @param listener リスナー
     */
    public void startCollision(final DeviceInfo device, final DeviceCollisionListener listener) {
        synchronized (device) {
            if (!device.isCollisionStarted()) {
                device.startCollistion(new DeviceCollisionListener() {
                    @Override
                    public void collisionDetected(final DeviceInfo info, final CollisionDetectedAsyncData data) {
                        if (listener != null) {
                            listener.collisionDetected(info, data);
                        }
                        if (!hasCollisionListener(device.getDevice().getUniqueId())) {
                            stopCollision(device);
                        }
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

        List<Event> eventQua = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(),
                SpheroProfile.PROFILE_NAME, SpheroProfile.INTER_QUATERNION, SpheroProfile.ATTR_ON_QUATERNION);

        List<Event> eventOri = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(),
                DeviceOrientationProfile.PROFILE_NAME, null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        List<Event> eventLoc = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(),
                SpheroProfile.PROFILE_NAME, SpheroProfile.INTER_LOCATOR, SpheroProfile.ATTR_ON_LOCATOR);

        return (eventOri.size() != 0) || (eventQua.size() != 0) || (eventLoc.size() != 0);
    }
    
    /**
     * バックライトを点滅させる.
     * 
     * @param info デバイス情報
     * @param intensity 明るさ
     * @param pattern パターン
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
        info.getDevice().executeMacro(m);
    }
    
    /**
     * フロントライトを点滅させる.
     * 
     * @param info デバイス情報
     * @param colors 色
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
        info.getDevice().executeMacro(m);
    }

    /**
     * 指定されたデータからOrientationデータを作成する.
     * @param data データ
     * @param interval インターバル
     * @return Orientationデータ
     */
    public static Bundle createOrientation(final DeviceSensorsData data, final long interval)  {
        Acceleration accData = data.getAccelerometerData().getFilteredAcceleration();
        Bundle accelerationIncludingGravity = new Bundle();
        // Spheroでは単位がG(1G=9.81m/s^2)で正規化しているので、Device Connectの単位(m/s^2)に変換する。
        DeviceOrientationProfile.setX(accelerationIncludingGravity, accData.x * G);
        DeviceOrientationProfile.setY(accelerationIncludingGravity, accData.y * G);
        DeviceOrientationProfile.setZ(accelerationIncludingGravity, accData.z * G);

        GyroData gyroData = data.getGyroData();
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
     * @param data データ
     * @param interval インターバル
     * @return Quaternionデータ
     */
    public static Bundle createQuaternion(final DeviceSensorsData data, final long interval) {
        QuaternionSensor quat = data.getQuaternion();
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
     * @param data データ
     * @return Locatorデータ
     */
    public static Bundle createLocator(final DeviceSensorsData data) {
        LocatorData loc = data.getLocatorData();
        Bundle locator = new Bundle();
        locator.putFloat(SpheroProfile.PARAM_POSITION_X, loc.getPositionX());
        locator.putFloat(SpheroProfile.PARAM_POSITION_Y, loc.getPositionY());
        locator.putFloat(SpheroProfile.PARAM_VELOCITY_X, loc.getVelocityX());
        locator.putFloat(SpheroProfile.PARAM_VELOCITY_Y, loc.getVelocityY());
        return locator;
    }

    /**
     * 指定されたデータからCollisionデータを作成する.
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
        
        CollisionPower power = data.getImpactPower();
        Bundle impactPower = new Bundle();
        impactPower.putShort(SpheroProfile.PARAM_X, power.x);
        impactPower.putShort(SpheroProfile.PARAM_Y, power.y);
        
        collision.putBundle(SpheroProfile.PARAM_IMPACT_ACCELERATION, impactAcceleration);
        collision.putBundle(SpheroProfile.PARAM_IMPACT_AXIS, impactAxis);
        collision.putBundle(SpheroProfile.PARAM_IMPACT_POWER, impactPower);
        collision.putFloat(SpheroProfile.PARAM_IMPACT_SPEED, data.getImpactSpeed());
        collision.putLong(SpheroProfile.PARAM_IMPACT_TIMESTAMP, data.getTimeStamp().getTime());
        return collision;
    }

    /**
     * Spheroが接続された時の処理.
     * 
     * @param sphero 接続されたSphero
     */
    private void onConnected(final Sphero sphero) {
        DeviceInfo info = new DeviceInfo();
        info.setDevice(sphero);
        info.setBackBrightness(1.f);
        sphero.enableStabilization(true);
        if (BuildConfig.DEBUG) {
            Log.d("", "connected device : " + sphero.toString());
        }
        mDevices.put(sphero.getUniqueId(), info);
    }

    /**
     * センサーのイベントが登録されているか確認する.
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
        if (events != null && events.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * 衝突のイベントが登録されているか確認する.
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
    private class DiscoveryListenerImpl implements DiscoveryListener {

        /**
         * 新規に検知したもの.
         */
        private List<Sphero> mFounds;

        /**
         * 消失したもの.
         */
        private List<Sphero> mLosts;

        /**
         * インスタンスを生成する.
         */
        public DiscoveryListenerImpl() {
            mFounds = new ArrayList<Sphero>();
            mLosts = new ArrayList<Sphero>();
        }

        @Override
        public void discoveryComplete(final List<Sphero> spheros) {
            synchronized (SpheroManager.this) {
                checkChangeAndSendDiscoveryMessage(spheros);
                // 検知終了時に最終的に検知しているデバイスを保持しておく
                mFoundDevices = spheros;
            }
        }

        @Override
        public void onBluetoothDisabled() {
            stopDiscovery();
            
            mFounds.clear();
            mLosts.clear();
            mDiscoveryListener.onDeviceLostAll();
        }

        @Override
        public void onFound(final List<Sphero> spheros) {
            synchronized (SpheroManager.this) {
                checkChangeAndSendDiscoveryMessage(spheros);
                mFoundDevices = spheros;
            }
        }

        /**
         * 差分をチェックして、通知をおくる.
         * 
         * @param spheros 新規に取得されたデバイス一覧
         */
        private void checkChangeAndSendDiscoveryMessage(final List<Sphero> spheros) {
            if (mDiscoveryListener != null) {
                if (mFoundDevices != null) {
                    for (Sphero s : spheros) {
                        if (!mFoundDevices.contains(s)) {
                            mFounds.add(s);
                        }
                    }

                    for (Sphero s : mFoundDevices) {
                        if (!spheros.contains(s)) {
                            mLosts.add(s);
                        }
                    }
                } else {
                    mFounds.addAll(spheros);
                }

                for (Sphero s : mFounds) {
                    if (BuildConfig.DEBUG) {
                        Log.d("", "============================");
                        Log.d("", "found         : " + s);
                        Log.d("", "connected     : " + s.isConnected());
                        Log.d("", "known         : " + s.isKnown());
                        Log.d("", "corrupt       : " + s.isMainAppCorrupt());
                        Log.d("", "under control : " + s.isUnderControl());
                        Log.d("", "============================");
                    }
                    mDiscoveryListener.onDeviceFound(s);
                }

                for (Sphero s : mLosts) {
                    if (BuildConfig.DEBUG) {
                        Log.d("", "============================");
                        Log.d("", "lost          : " + s);
                        Log.d("", "connected     : " + s.isConnected());
                        Log.d("", "known         : " + s.isKnown());
                        Log.d("", "corrupt       : " + s.isMainAppCorrupt());
                        Log.d("", "under control : " + s.isUnderControl());
                        Log.d("", "============================");
                    }
                    mDiscoveryListener.onDeviceLost(s);
                }

                mFounds.clear();
                mLosts.clear();
            }
        }
    }

    /**
     * 接続リスナー.
     */
    private class ConnectionListenerImpl implements ConnectionListener {

        @Override
        public void onConnected(final Robot robot) {
            if (robot instanceof Sphero) {
                SpheroManager.this.onConnected((Sphero) robot);
                if (BuildConfig.DEBUG) {
                    Log.d("", "onConnected!");
                }
                synchronized (mConnLock) {
                    mConnLock.notifyAll();
                }
            }
        }

        @Override
        public void onConnectionFailed(final Robot robot) {
            // TODO 接続失敗の通知を出す
            if (BuildConfig.DEBUG) {
                Log.d("", "connect failed : " + robot);
            }
            synchronized (mConnLock) {
                mConnLock.notifyAll();
            }
        }

        @Override
        public void onDisconnected(final Robot robot) {
            mDevices.remove(robot.getUniqueId());
            if (BuildConfig.DEBUG) {
                Log.d("", "onDisconnected!");
            }
            if (mDiscoveryListener != null) {
                mDiscoveryListener.onDeviceLost((Sphero) robot);
            }
        }
    }

    /**
     * デバイス検知の通知を受けるリスナー.
     */
    public interface DeviceDiscoveryListener {

        /**
         * 見つかったデバイスを通知します.
         * 
         * @param sphero 見つかったデバイス
         */
        void onDeviceFound(Sphero sphero);

        /**
         * 消失したデバイスの通知を受けるリスナー.
         * 
         * @param sphero 消失したデバイス
         */
        void onDeviceLost(Sphero sphero);
        
        /**
         * すべてのデバイスの消失を通知します.
         */
        void onDeviceLostAll();
    }

    @Override
    public void sensorUpdated(final DeviceInfo info, final DeviceSensorsData data, final long interval) {

        if (mService == null) {
            return;
        }

        mCacheDeviceInfo = info;
        mCacheDeviceSensorsData = data;
        mCacheInterval = interval;

        List<Event> events = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(),
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

        events = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(), SpheroProfile.PROFILE_NAME,
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

        events = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(), SpheroProfile.PROFILE_NAME,
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

        List<Event> events = EventManager.INSTANCE.getEventList(info.getDevice().getUniqueId(),
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
}
