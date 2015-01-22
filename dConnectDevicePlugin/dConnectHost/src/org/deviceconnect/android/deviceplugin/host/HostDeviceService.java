/*
 HostDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;
import org.deviceconnect.android.deviceplugin.host.camera.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.host.manager.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.profile.HostBatteryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostConnectProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileDescriptorProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaPlayerProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaStreamingRecordingProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostNetworkServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostNotificationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostPhoneProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostProximityProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSettingsProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSystemProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostVibrationProfile;
import org.deviceconnect.android.deviceplugin.host.video.VideoConst;
import org.deviceconnect.android.deviceplugin.host.video.VideoPlayer;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.FileDescriptorProfile;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.profile.NetworkServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.FileDescriptorProfileConstants.Flag;
import org.deviceconnect.profile.PhoneProfileConstants.CallState;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

/**
 * Host Device Service.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceService extends DConnectMessageService implements SensorEventListener {
    /** マルチキャスト用のタグ. */
    private static final String HOST_MULTICAST = "deviceplugin.host";

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** 独自エラーコード. */
    private static final int UNIQUE_ERROR_CODE = 101;

    /** バッファーサイズ. */
    private static final int BUFFER_SIZE = 1024;

    /** SensorManager. */
    private SensorManager mSensorManager;

    /** SensorManager. */
    private SensorManager mSensorManagerProximity;

    /** DeviceID. */
    private String mDeviceId;

    /** ServiceのList. */
    private List<Bundle> services;

    /** バッテリー関連の処理と値処理. */
    private HostBatteryManager mHostBatteryManager;

    /** ミリ秒 - 秒オーダー変換用. */
    private static final int UNIT_SEC = 1000;
    
    /** Video Current Position response.*/
    private Intent mResponse = null;
    
    /** Intent filter for battery charge event. */
    private IntentFilter ifBatteryCharge;

    /** Intent filter for battery connect event. */
    private IntentFilter ifBatteryConnect;

    @Override
    public void onCreate() {

        super.onCreate();

        // EventManagerの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // LocalOAuthの処理
        LocalOAuth2Main.initialize(getApplicationContext());

        // ファイル管理クラスの作成
        mFileMgr = new FileManager(this);

        // add supported profiles
        addProfile(new HostConnectProfile(BluetoothAdapter.getDefaultAdapter()));
        addProfile(new HostNotificationProfile());
        addProfile(new HostDeviceOrientationProfile());
        addProfile(new HostBatteryProfile());
        addProfile(new HostMediaStreamingRecordingProfile());
        addProfile(new HostPhoneProfile());
        addProfile(new HostSettingsProfile());
        addProfile(new HostMediaPlayerProfile());
        addProfile(new HostFileProfile(mFileMgr));
        addProfile(new HostFileDescriptorProfile());
        addProfile(new HostVibrationProfile());
        addProfile(new HostProximityProfile());

        // バッテリー関連の処理と値の保持
        mHostBatteryManager = new HostBatteryManager();
        mHostBatteryManager.getBatteryInfo(this.getContext());

        ifBatteryCharge = new IntentFilter();
        ifBatteryCharge.addAction(Intent.ACTION_BATTERY_CHANGED);
        ifBatteryCharge.addAction(Intent.ACTION_BATTERY_LOW);
        ifBatteryCharge.addAction(Intent.ACTION_BATTERY_OKAY);

        ifBatteryConnect = new IntentFilter();
        ifBatteryConnect.addAction(Intent.ACTION_POWER_CONNECTED);
        ifBatteryConnect.addAction(Intent.ACTION_POWER_DISCONNECTED);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
        if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Phone
            List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId,
                    HostPhoneProfile.PROFILE_NAME,
                    null,
                    HostPhoneProfile.ATTRIBUTE_ON_CONNECT);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostPhoneProfile.setAttribute(mIntent, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);
                Bundle phoneStatus = new Bundle();
                HostPhoneProfile.setPhoneNumber(phoneStatus, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                HostPhoneProfile.setState(phoneStatus, CallState.START);
                HostPhoneProfile.setPhoneStatus(mIntent, phoneStatus);
                getContext().sendBroadcast(mIntent);
            }
            return START_STICKY;
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                || WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            // Wifi
            List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId,
                    HostConnectProfile.PROFILE_NAME,
                    null,
                    HostConnectProfile.ATTRIBUTE_ON_WIFI_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostConnectProfile.setAttribute(mIntent, HostConnectProfile.ATTRIBUTE_ON_WIFI_CHANGE);
                Bundle wifiConnecting = new Bundle();
                WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
                HostConnectProfile.setEnable(wifiConnecting, wifiMgr.isWifiEnabled());
                HostConnectProfile.setConnectStatus(mIntent, wifiConnecting);
                getContext().sendBroadcast(mIntent);
            }
            return START_STICKY;
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId,
                    HostConnectProfile.PROFILE_NAME,
                    null,
                    HostConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostConnectProfile.setAttribute(mIntent, HostConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
                Bundle bluetoothConnecting = new Bundle();
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                HostConnectProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
                HostConnectProfile.setConnectStatus(mIntent, bluetoothConnecting);
                getContext().sendBroadcast(mIntent);
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Register broadcast receiver for battery charge event. 
     */
    public void registerBatteryChargeBroadcastReceiver() {
        registerReceiver(mBatteryChargeBR, ifBatteryCharge);
    }

    /**
     * Unregister broadcast receiver for battery charge event. 
     */
    public void unregisterBatteryChargeBroadcastReceiver() {
        unregisterReceiver(mBatteryChargeBR);
    }

    /**
     * Register broadcast receiver for battery connect event. 
     */
    public void registerBatteryConnectBroadcastReceiver() {
        registerReceiver(mBatteryConnectBR, ifBatteryConnect);
    }

    /**
     * Unregister broadcast receiver for battery connect event. 
     */
    public void unregisterBatteryConnectBroadcastReceiver() {
        unregisterReceiver(mBatteryConnectBR);
    }

    /**
     * Broadcast receiver for battery charge event. 
     */
    private BroadcastReceiver mBatteryChargeBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_BATTERY_LOW.equals(action)
                    || Intent.ACTION_BATTERY_OKAY.equals(action)) {
                // バッテリーが変化した時
                mHostBatteryManager.setBatteryRequest(intent);
                List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId, HostBatteryProfile.PROFILE_NAME,
                        null, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent mIntent = EventManager.createEventMessage(event);
                    HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);
                    Bundle battery = new Bundle();
                    double level = ((double) (mHostBatteryManager.getBatteryLevel())) / ((double) getBatteryScale());
                    HostBatteryProfile.setLevel(battery, level);
                    HostBatteryProfile.setBattery(mIntent, battery);
                    getContext().sendBroadcast(mIntent);
                }
            }
        }
    };

    /**
     * Broadcast receiver for battery connect event. 
     */
    private BroadcastReceiver mBatteryConnectBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                // バッテリーが充電された時
                mHostBatteryManager.setBatteryRequest(intent);
                List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId, HostBatteryProfile.PROFILE_NAME,
                        null, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent mIntent = EventManager.createEventMessage(event);
                    HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);
                    Bundle charging = new Bundle();
                    if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                        HostBatteryProfile.setCharging(charging, true);
                    } else {
                        HostBatteryProfile.setCharging(charging, false);
                    }
                    HostBatteryProfile.setBattery(mIntent, charging);
                    getContext().sendBroadcast(mIntent);
                }
            }
        }
    };

    @Override
    protected SystemProfile getSystemProfile() {
        return new HostSystemProfile(this);
    }

    @Override
    protected NetworkServiceDiscoveryProfile getNetworkServiceDiscoveryProfile() {
        return new HostNetworkServiceDiscoveryProfile();
    }

    /**
     * DeviceIDを設定.
     * 
     * @param deviceId デバイスID
     */
    public void setDeviceId(final String deviceId) {
        mDeviceId = deviceId;
    }

    /**
     * Battery Profile<br>
     * バッテリーレベルを取得.
     * 
     * @return バッテリーレベル
     */
    public int getBatteryLevel() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryLevel();
    }

    /**
     * Battery Profile<br>
     * バッテリーステータスを取得.
     * 
     * @return バッテリーレベル
     */
    public int getBatteryStatus() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryStatus();
    }

    /**
     * Battery Profile<br>
     * バッテリーレベルを取得.
     * 
     * @return バッテリーレベル
     */
    public int getBatteryScale() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryScale();
    }

    //
    // Device Orientation Profile
    //
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
     * Device Orientation Profile<br>
     * イベントの登録.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param sessionKey セッションキー
     */
    public void registerDeviceOrientationEvent(final Intent response, final String deviceId, final String sessionKey) {

        mDeviceId = deviceId;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            mAccelStartTime = System.currentTimeMillis();
        }

        sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnDeviceOrientation event");
        sendBroadcast(response);
    }

    /**
     * Promity Profile<br>
     * イベントの登録.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param sessionKey セッションキー
     */
    public void registerPromityEvent(final Intent response, final String deviceId, final String sessionKey) {

        mDeviceId = deviceId;
        mSensorManagerProximity = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManagerProximity.getSensorList(Sensor.TYPE_PROXIMITY);

        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            mSensorManagerProximity.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register onuserproximity event");
        sendBroadcast(response);
    }

    /**
     * Promity Profile<br>
     * イベントの解除.
     * 
     * @param response レスポンス
     */
    public void unregisterPromityEvent(final Intent response) {
        mSensorManagerProximity.unregisterListener(this);
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister onuserproximity event");
        sendBroadcast(response);
    }

    /**
     * Device Orientation Profile<br>
     * イベントの解除.
     * 
     * @param response レスポンス
     */
    public void unregisterDeviceOrientationEvent(final Intent response) {
        mSensorManager.unregisterListener(this);
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnDeviceOrientation event");
        sendBroadcast(response);
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            mAccellX = sensorEvent.values[0];
            mAccellY = sensorEvent.values[1];
            mAccellZ = sensorEvent.values[2];

            long interval = System.currentTimeMillis() - mAccelStartTime;

            Bundle orientation = new Bundle();
            Bundle a1 = new Bundle();
            a1.putDouble(DeviceOrientationProfile.PARAM_X, 0.0);
            a1.putDouble(DeviceOrientationProfile.PARAM_Y, 0.0);
            a1.putDouble(DeviceOrientationProfile.PARAM_Z, 0.0);
            Bundle a2 = new Bundle();
            a2.putDouble(DeviceOrientationProfile.PARAM_X, mAccellX);
            a2.putDouble(DeviceOrientationProfile.PARAM_Y, mAccellY);
            a2.putDouble(DeviceOrientationProfile.PARAM_Z, mAccellZ);
            Bundle r = new Bundle();
            r.putDouble(DeviceOrientationProfile.PARAM_ALPHA, mGyroX);
            r.putDouble(DeviceOrientationProfile.PARAM_BETA, mGyroY);
            r.putDouble(DeviceOrientationProfile.PARAM_GAMMA, mGyroZ);
            orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION, a1);
            orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION_INCLUDING_GRAVITY, a2);
            orientation.putBundle(DeviceOrientationProfile.PARAM_ROTATION_RATE, r);
            orientation.putLong(DeviceOrientationProfile.PARAM_INTERVAL, 0);
            DeviceOrientationProfile.setInterval(orientation, interval);

            List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId, DeviceOrientationProfile.PROFILE_NAME,
                    null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
                getContext().sendBroadcast(intent);
            }

            mAccelStartTime = System.currentTimeMillis();
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyroX = sensorEvent.values[0];
            mGyroY = sensorEvent.values[1];
            mGyroZ = sensorEvent.values[2];
        }
    }

    //
    // File Descriptor Profile
    //

    /** File Output Stream. */
    private FileOutputStream mFos;

    /** File Input Stream. */
    private FileInputStream mFis;

    /**
     * FileがオープンかどうかのFlag.<br>
     * 開いている:true, 開いていない: false
     */
    private Boolean mFileOpenFlag = false;

    /** Fileネームを保持する変数. */
    private String mFileName = "";

    /** EventのFlag. */
    private boolean onWatchfileEventFlag = false;

    /**
     * 現在の更新時間.<br>
     * 更新時間の定義は、open, write, readが実施されたタイミング
     */
    private long mFileDescriptorCurrentSystemTime;

    /** 現在の更新時間. */
    private String mFileDescriptorCurrentTime = "";

    /** FileDescriptorの開いているファイルのPath. */
    private String mFileDescriptorPath = "";

    /** FileDescripor管理用DeviceId. */
    private String mFileDescriptorDeviceId = "";

    /** File mode. */
    private Flag mFlag = null;

    /** 更新可能間隔(1分). */
    private static final int AVAILABLE_REWRITE_TIME = 60000;

    /**
     * 更新可能かどうかの判定.
     * 
     * @return 更新可能な場合はtrue
     */
    private boolean checkUpdate() {
        return System.currentTimeMillis() - mFileDescriptorCurrentSystemTime > AVAILABLE_REWRITE_TIME;
    }

    /**
     * ファイルを開く.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param path パス
     * @param flag ファイルが開かれているかどうかのフラグ
     */
    public void openFile(final Intent response, final String deviceId, final String path, final Flag flag) {

        if (!mFileOpenFlag || checkUpdate()) {
            try {
                mFileDescriptorCurrentSystemTime = System.currentTimeMillis();
                mFileOpenFlag = true;
                File mBaseDir = mFileMgr.getBasePath();
                mFileDescriptorPath = path;
                if (!mFileDescriptorPath.startsWith("/")) {
                    mFileDescriptorPath = "/" + path;
                }

                mFos = new FileOutputStream(new File(mBaseDir + mFileDescriptorPath), true);
                mFis = new FileInputStream(new File(mBaseDir + mFileDescriptorPath));

                mFlag = flag;
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "Open file:" + Environment.getExternalStorageDirectory()
                        + path);
                sendBroadcast(response);

                mFileName = path;
            } catch (FileNotFoundException e) {
                mFileOpenFlag = false;
                MessageUtils.setUnknownError(response, "Can not open file:" + path + ":" + e);
                sendBroadcast(response);

                mFileName = "";
            }
        } else {
            MessageUtils.setError(response, UNIQUE_ERROR_CODE, "Opening another file");
            sendBroadcast(response);
            if (mFos != null) {
                try {
                    mFos.close();
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                mFos = null;
            }
            if (mFis != null) {
                try {
                    mFis.close();
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                mFis = null;
            }
        }
    }

    /**
     * ファイルに書き込みする.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param path パス
     * @param data データ
     * @param position 書き込みポイント
     */
    public void writeDataToFile(final Intent response, final String deviceId, final String path, final byte[] data,
            final Long position) {
        int pos = 0;
        if (position != null) {
            pos = (int) position.longValue();
        }
        if (pos < 0 || data.length < pos) {
            MessageUtils.setInvalidRequestParameterError(response, "invalid position");
            sendBroadcast(response);
            return;
        }
        if (mFileOpenFlag && mFileName.equals(path)) {
            try {
                if (mFlag.equals(Flag.RW)) {
                    // 現在の時刻を取得
                    mFileDescriptorCurrentSystemTime = System.currentTimeMillis();
                    Date date = new Date();
                    SimpleDateFormat mDateFormat = new SimpleDateFormat(
                            "yyyy'-'MM'-'dd' 'kk':'mm':'ss'+0900'", Locale.getDefault());
                    mFileDescriptorCurrentTime = mDateFormat.format(date);
                    mFos.write(data, pos, data.length - pos);
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "Write data:" + path);
                    sendBroadcast(response);
                    sendFileDescriptorOnWatchfileEvent();
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "Read mode only");
                    sendBroadcast(response);
                }

            } catch (Exception e) {
                MessageUtils.setUnknownError(response, "Can not write data:" + path + e);
                sendBroadcast(response);
            }
        } else {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
            response.putExtra(DConnectMessage.EXTRA_VALUE, "Can not write data:" + path);
            sendBroadcast(response);
        }
    }

    /**
     * File Descriptor Profile<br>
     * ファイルを読む.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param path パス
     * @param position 書き込みポジション
     * @param length 長さ
     */
    public void readFile(final Intent response, final String deviceId, final String path, final long position,
            final long length) {
        if (position < 0) {
            MessageUtils.setInvalidRequestParameterError(response, "invalid position");
            sendBroadcast(response);
            return;
        }

        File mBaseDir = mFileMgr.getBasePath();
        if (mFileOpenFlag && mFileName.equals(path)) {
            try {
                mFileDescriptorCurrentSystemTime = System.currentTimeMillis();
                StringBuffer fileContent = new StringBuffer("");
                byte[] buffer = new byte[BUFFER_SIZE];
                int nCount = 0;
                String paths = path;
                if (!path.startsWith("/")) {
                    paths = "/" + path;
                }
                FileInputStream fis = new FileInputStream(mBaseDir + paths);
                while (((fis.read(buffer, 0, 1)) != -1) && nCount < position + length) {
                    if (nCount >= position) {
                        fileContent.append(new String(buffer, 0, 1));
                    }
                    nCount++;
                }
                fis.close();

                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                response.putExtra(FileDescriptorProfile.PARAM_SIZE, length);
                response.putExtra(FileDescriptorProfile.PARAM_FILE_DATA, fileContent.toString());
                sendBroadcast(response);

            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            MessageUtils.setUnknownError(response, "Can not read data:" + path);
            sendBroadcast(response);
        }
    }

    /**
     * Fileを閉じる.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param path パス
     */
    public void closeFile(final Intent response, final String deviceId, final String path) {

        // fileNameが一致した場合のみ閉じる
        if (mFileOpenFlag && mFileName.equals(path)) {
            try {
                mFileDescriptorCurrentSystemTime = 0;
                mFos.close();
                mFileOpenFlag = false;

                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "Close file:" + path);
                sendBroadcast(response);
                mFileName = "";

            } catch (IOException e) {
                mFileOpenFlag = false;
                MessageUtils.setUnknownError(response, "Can not close file:" + path + e);
                sendBroadcast(response);
            }
        } else {
            mFileOpenFlag = false;
            MessageUtils.setUnknownError(response, "Can not close file:" + path);
            sendBroadcast(response);
        }
    }

    /**
     * OnWatchFileEventの登録.
     * 
     * @param deviceId デバイスID
     */
    public void registerFileDescriptorOnWatchfileEvent(final String deviceId) {
        onWatchfileEventFlag = true;
        mFileDescriptorDeviceId = deviceId;
    }

    /**
     * OnWatchFileEventの削除.
     */
    public void unregisterFileDescriptorOnWatchfileEvent() {
        onWatchfileEventFlag = false;
    }

    /**
     * 状態変化のイベントを通知.
     */
    public void sendFileDescriptorOnWatchfileEvent() {
        if (onWatchfileEventFlag) {
            List<Event> events = EventManager.INSTANCE.getEventList(
                    mFileDescriptorDeviceId,
                    HostFileDescriptorProfile.PROFILE_NAME,
                    null,
                    HostFileDescriptorProfile.ATTRIBUTE_ON_WATCH_FILE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);

                HostFileDescriptorProfile.setAttribute(intent, FileDescriptorProfile.ATTRIBUTE_ON_WATCH_FILE);
                Bundle fileDescriptor = new Bundle();
                FileDescriptorProfile.setPath(fileDescriptor, mFileDescriptorPath);
                FileDescriptorProfile.setCurr(fileDescriptor, mFileDescriptorCurrentTime);
                FileDescriptorProfile.setPrev(fileDescriptor, "");
                intent.putExtra(FileDescriptorProfile.PARAM_FILE_DATA, fileDescriptor);
                intent.putExtra(FileDescriptorProfile.PARAM_PROFILE, FileDescriptorProfile.PROFILE_NAME);
                getContext().sendBroadcast(intent);
            }
        }
    }

    // ----------------------------------------------
    // MediaPlayer Profile
    // ----------------------------------------------
    /** MediaPlayerのインスタンス. */
    private MediaPlayer mMediaPlayer;
    /** Mediaのステータス. */
    private int mMediaStatus = 0;
    /** Mediaが未設定. */
    private static final int MEDIA_PLAYER_NODATA = 0;
    /** Mediaがセット. */
    private static final int MEDIA_PLAYER_SET = 1;
    /** Mediaが再生中. */
    private static final int MEDIA_PLAYER_PLAY = 2;
    /** Mediaが一時停止中. */
    private static final int MEDIA_PLAYER_PAUSE = 3;
    /** Mediaが停止. */
    private static final int MEDIA_PLAYER_STOP = 4;
    /** Mediaが再生完了. */
    private static final int MEDIA_PLAYER_COMPLETE = 5;
    /** MEDIAタイプ(動画). */
    private static final int MEDIA_TYPE_VIDEO = 1;
    /** MEDIAタイプ(音楽). */
    private static final int MEDIA_TYPE_MUSIC = 2;
//    /** MEDIAタイプ(音声). */
//    private static final int MEDIA_TYPE_AUDIO = 3;
    /** Media Status. */
    private int mSetMediaType = 0;
    /** onStatusChange Eventの状態. */
    private boolean onStatusChangeEventFlag = false;
    /** 現在再生中のファイルパス. */
    private String myCurrentFilePath = "";
    /** 現在再生中のファイルパス. */
    private String myCurrentFileMIMEType = "";
    /** 現在再生中のPosition. */
    private int myCurrentMediaPosition = 0;
    
    /**
     * サポートしているaudioのタイプ一覧.
     */
    private static final List<String> AUDIO_TYPE_LIST = Arrays.asList("audio/mpeg",
            "audio/x-wav", "application/ogg", "audio/x-ms-wma", 
            "audio/mp3", "audio/ogg", "audio/mp4");
    
    /**
     * サポートしているvideoのタイプ一覧.
     */
    private static final List<String> VIDEO_TYPE_LIST = Arrays.asList("video/3gpp",
            "video/mp4", "video/m4v", "video/3gpp2", "video/mpeg");

    /**
     * 再生するメディアをセットする(Idから).
     * 
     * @param response レスポンス
     * @param mediaId MediaID
     */
    public void putMediaId(final Intent response, final String mediaId) {
        // Videoとしてパスを取得
        Uri mUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));

        String filePath = getPathFromUri(mUri);

        // nullなら、Audioとしてパスを取得
        if (filePath == null) {
            mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));
            filePath = getPathFromUri(mUri);
        }

        String mMineType = getMIMEType(filePath);

        // パス指定の場合
        if (AUDIO_TYPE_LIST.contains(mMineType)) {
            mMediaPlayer = new MediaPlayer();

            try {
                mSetMediaType = MEDIA_TYPE_MUSIC;
                myCurrentFilePath = filePath;
                myCurrentFileMIMEType = mMineType;
                mMediaStatus = MEDIA_PLAYER_SET;
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(final MediaPlayer arg0) {
                        mMediaStatus = MEDIA_PLAYER_COMPLETE;
                        sendOnStatusChangeEvent("complete");
                    }
                });
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mp) {
                    }
                });
                
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "regist:" + filePath);
                sendOnStatusChangeEvent("media");
                sendBroadcast(response);
            } catch (IOException e) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not regist:" + filePath);
                sendBroadcast(response);
            }
        } else if (VIDEO_TYPE_LIST.contains(mMineType)) {
            try {

                mSetMediaType = MEDIA_TYPE_VIDEO;
                myCurrentFilePath = filePath;
                myCurrentFileMIMEType = mMineType;

                mMediaPlayer = new MediaPlayer();
                FileInputStream fis = null;
                FileDescriptor mFd = null;

                fis = new FileInputStream(myCurrentFilePath);
                mFd = fis.getFD();

                mMediaPlayer.setDataSource(mFd);
                mMediaPlayer.prepare();
                mMediaPlayer.release();
                fis.close();

                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "regist:" + filePath);
                sendOnStatusChangeEvent("media");
                sendBroadcast(response);
            } catch (Exception e) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not mount:" + filePath);
                sendBroadcast(response);
            }
        } else {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
            response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not open:" + filePath);
            sendBroadcast(response);
        }
    }

    /**
     * onStatusChange Eventの登録.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     */
    public void registerOnStatusChange(final Intent response, final String deviceId) {
        mDeviceId = deviceId;
        onStatusChangeEventFlag = true;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnStatusChange event");
        sendBroadcast(response);
    }

    /**
     * onStatusChange Eventの解除.
     * 
     * @param response レスポンス
     */
    public void unregisterOnStatusChange(final Intent response) {
        onStatusChangeEventFlag = false;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnStatusChange event");
        sendBroadcast(response);
    }

    /**
     * 状態変化のイベントを通知.
     * 
     * @param status ステータス
     */
    public void sendOnStatusChangeEvent(final String status) {

        if (onStatusChangeEventFlag) {
            List<Event> events = EventManager.INSTANCE.getEventList(mDeviceId,
                    MediaPlayerProfile.PROFILE_NAME,
                    null,
                    MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);

            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);

            double maxVolume = 1;
            double mVolume = 0;

            mVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            double mVolumeValue = mVolume / maxVolume;

            for (int i = 0; i < events.size(); i++) {

                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);

                MediaPlayerProfile.setAttribute(intent, MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);
                Bundle mediaPlayer = new Bundle();
                MediaPlayerProfile.setStatus(mediaPlayer, status);
                MediaPlayerProfile.setMediaId(mediaPlayer, myCurrentFilePath);
                MediaPlayerProfile.setMIMEType(mediaPlayer, myCurrentFileMIMEType);
                MediaPlayerProfile.setPos(mediaPlayer, myCurrentMediaPosition / UNIT_SEC);
                MediaPlayerProfile.setVolume(mediaPlayer, mVolumeValue);
                MediaPlayerProfile.setMediaPlayer(intent, mediaPlayer);
                getContext().sendBroadcast(intent);
            }
        }
    }

    /**
     * URIからパスを取得.
     * 
     * @param mUri URI
     * @return パス
     */
    private String getPathFromUri(final Uri mUri) {
        try {
            Cursor c = getContentResolver().query(mUri, null, null, null, null);
            c.moveToFirst();
            String filename = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            return filename;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Mediaの再再生.
     * 
     * @return SessionID
     */
    public int resumeMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                mMediaPlayer.start();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PLAY;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_RESUME);
            this.getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("play");
            return 0;
        }
        return 0;
    }

    /**
     * メディアの再生.
     * 
     * @return セッションID
     */
    public int playMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                if (mMediaStatus != MEDIA_PLAYER_PAUSE && mMediaStatus != MEDIA_PLAYER_SET 
                        && mMediaStatus != MEDIA_PLAYER_COMPLETE) {
                    mMediaPlayer.prepare();
                }
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    mMediaPlayer.seekTo(0);
                }
                mMediaPlayer.start();
                mMediaStatus = MEDIA_PLAYER_PLAY;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            String className = getClassnameOfTopActivity();

            if (VideoPlayer.class.getName().equals(className)) {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                this.getContext().sendBroadcast(mIntent);
                sendOnStatusChangeEvent("play");

            } else {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.setClass(getContext(), VideoPlayer.class);
                Uri data = Uri.parse(myCurrentFilePath);
                mIntent.setDataAndType(data, myCurrentFileMIMEType);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
                sendOnStatusChangeEvent("play");
            }

            return 0;
        } else {
            return 0;
        }
    }

    /**
     * メディアの停止.
     * 
     * @return セッションID
     */
    public int pauseMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaStatus = MEDIA_PLAYER_PAUSE;
                mMediaPlayer.pause();

            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("pause");
            return mMediaPlayer.getAudioSessionId();

        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PAUSE);
            this.getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("pause");
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * ポジションを返す.
     * 
     * @return 現在のポジション
     */
    public int getMediaPos() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            return mMediaPlayer.getCurrentPosition() / UNIT_SEC;
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            String className = getClassnameOfTopActivity();
            if (VideoPlayer.class.getName().equals(className)) {
                // ReceiverをRegister
                IntentFilter mIntentFilter = new IntentFilter();
                mIntentFilter.addAction(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP);
                registerReceiver(mReceiver, mIntentFilter);

                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_GET_POS);
                this.getContext().sendBroadcast(mIntent);
                return Integer.MAX_VALUE;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Video ポジションを返す為のIntentを設定.
     * 
     * @param response 応答用Intent.
     */
    public void setVideoMediaPosRes(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mResponse = response;
        }
    }

    /**
     * VideoPlayer用Broadcast Receiver.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP)) {
                String mVideoAction = intent.getStringExtra(VideoConst.EXTRA_NAME);

                if (mVideoAction.equals(VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY_POS)) {
                    myCurrentMediaPosition = intent.getIntExtra("pos", 0);
                    mResponse.putExtra("pos", myCurrentMediaPosition / UNIT_SEC);
                    mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    sendBroadcast(mResponse);

                    // ReceiverをUnregister
                    unregisterReceiver(mReceiver);
                }
            }
        }
    };

    /**
     * ポジションを変える.
     * 
     * @param response レスポンス
     * @param pos ポジション　
     */
    public void setMediaPos(final Intent response, final int pos) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            mMediaPlayer.seekTo(pos * UNIT_SEC);
            myCurrentMediaPosition = pos * UNIT_SEC;
        } else {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_SEEK);
            mIntent.putExtra("pos", pos * UNIT_SEC);
            this.getContext().sendBroadcast(mIntent);
            myCurrentMediaPosition = pos * UNIT_SEC;
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendBroadcast(response);
    }

    /**
     * メディアの停止.
     */
    public void stopMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaPlayer.stop();
                mMediaStatus = MEDIA_PLAYER_STOP;
                sendOnStatusChangeEvent("stop");
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_STOP);
            this.getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("stop");
        }
    }

    /**
     * Play Status.
     * 
     * @param response レスポンス
     */
    public void getPlayStatus(final Intent response) {
        String mClassName = getClassnameOfTopActivity();

        // VideoRecorderの場合は、画面から消えている場合m
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);

            if (!VideoPlayer.class.getName().equals(mClassName)) {
                mMediaStatus = MEDIA_PLAYER_STOP;
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else {
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
                } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
                } else if (mMediaStatus == MEDIA_PLAYER_NODATA) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
                } else {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                }
            }
            sendBroadcast(response);
        } else {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            if (mMediaStatus == MEDIA_PLAYER_STOP) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
            } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
            } else if (mMediaStatus == MEDIA_PLAYER_NODATA) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
            } else {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            }
            sendBroadcast(response);
        }
    }

    // ================================
    // MediaStream_Recording
    // ================================

    /** Lock object. */
    private final Object mLockObj = new Object();

    /** Server for MotionJPEG. */
    private MixedReplaceMediaServer mServer;

    /**
     * Start a web server.
     * @return url of web server or null if this server cannot start.
     */
    public String startWebServer() {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                String ip = mServer.start();
                return ip;
            } else {
                return mServer.getUrl();
            }
        }
    }

    /**
     * Stop a web server.
     */
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
        }
    }

    /**
     * Cameraからのデータ受信用.
     */
    private IHostMediaStreamRecordingService.Stub mCameraService = new IHostMediaStreamRecordingService.Stub() {
        @Override
        public void sendPreviewData(final byte[] data, final int format, final int width, final int height) {
            synchronized (mLockObj) {
                if (mServer != null) {
                    mServer.offerMedia(data);
                }
            }
        }
    };

    /**
     * mDNSで端末検索.
     * 
     */
    private void searchDeviceByBonjour() {
        // cacheがfalseの場合は、検索開始
        // 初回検索,すでにデバイスがある場合, Wifi接続のBroadcastがある場合は入る
        new Thread(new Runnable() {
            public void run() {

                services = new ArrayList<Bundle>();

                android.net.wifi.WifiManager wifi =
                        (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
                WifiManager.MulticastLock lock = wifi.createMulticastLock(HOST_MULTICAST);
                lock.setReferenceCounted(true);
                lock.acquire();
            }
        }).start();

    }

    /**
     * mDNSで引っかかるように端末を起動.
     * 
     */
    private void invokeDeviceByBonjour() {
        // cacheがfalseの場合は、検索開始
        // 初回検索,すでにデバイスがある場合, Wifi接続のBroadcastがある場合は入る
        new Thread(new Runnable() {
            public void run() {

                services = new ArrayList<Bundle>();

                android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)
                            getSystemService(android.content.Context.WIFI_SERVICE);
                WifiManager.MulticastLock lock = wifi.createMulticastLock(HOST_MULTICAST);
                lock.setReferenceCounted(true);
                lock.acquire();
            }
        }).start();

    }

    /**
     * 端末のIPを取得.
     * 
     * @return 端末のIPアドレス
     */
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        String ipAddr = inetAddress.getHostAddress();
                        return ipAddr;
                    }
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        if ("camera".equals(intent.getAction())) {
            return mCameraService;
        } else {
            return mStub;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if ("camera".equals(intent.getAction())) {
            stopWebServer();
        }
        return super.onUnbind(intent);
    }

    /**
     * Host Device Pluginのサービス.
     */
    private IHostDeviceService.Stub mStub = new IHostDeviceService.Stub() {

        @Override
        public void registerCallback(final IHostDeviceCallback callback) throws RemoteException {
        }

        @Override
        public void unregisterCallback(final IHostDeviceCallback callback) throws RemoteException {
        }

        @Override
        public void searchHost() throws RemoteException {
            searchDeviceByBonjour();
        }

        @Override
        public int getHostStatus() throws RemoteException {
            return 0;
        }

        @Override
        public void invokeHost() throws RemoteException {
            invokeDeviceByBonjour();
        }

    };

    /**
     * onClickの登録.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param sessionKey セッションキー
     */
    public void registerOnConnect(final Intent response, final String deviceId, final String sessionKey) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register onClick event");
        sendBroadcast(response);
    }

    /**
     * onClickの削除.
     * 
     * @param response レスポンス
     * @param deviceId デバイスID
     * @param sessionKey セッションキー
     */
    public void unregisterOnConnect(final Intent response, final String deviceId, final String sessionKey) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister onClick event");
        sendBroadcast(response);
    }

    /**
     * 3GPのファイルがVideoかAudioかの判定.
     * 
     * @param mFile 判定したいURI
     * @return Videoならtrue, audioならfalse
     */
    private boolean checkVideo(final File mFile) {
        int height = 0;
        try {
            mMediaPlayer = new MediaPlayer();
            FileInputStream fis = null;
            FileDescriptor mFd = null;

            fis = new FileInputStream(mFile);
            mFd = fis.getFD();

            mMediaPlayer.setDataSource(mFd);
            mMediaPlayer.prepare();
            height = mMediaPlayer.getVideoHeight();
            mMediaPlayer.release();
            fis.close();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        return height > 0;
    }

    /**
     * ファイルからMIME Typeを取得.
     * 
     * @param path パス
     * @return MineType
     */
    private String getMIMEType(final String path) {
        // 空文字, 日本語対策, ファイル形式のStringを取得
        String mFilename = new File(path).getName();
        int dotPos = mFilename.lastIndexOf(".");
        String mFormat = mFilename.substring(dotPos, mFilename.length());
        // 拡張子を取得
        String mExt = MimeTypeMap.getFileExtensionFromUrl(mFormat);
        // 小文字に変換
        mExt = mExt.toLowerCase(Locale.getDefault());
        // MIME Typeを返す
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExt);
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     * 
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String mClassName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return mClassName;
    }
}
