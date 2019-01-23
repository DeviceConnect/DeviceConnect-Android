/*
 HostDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.BuildConfig;

import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.file.FileDataManager;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.HostMediaPlayerManager;
import org.deviceconnect.android.deviceplugin.host.profile.HostBatteryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostCanvasProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostConnectionProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostGeolocationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostLightProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaPlayerProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaStreamingRecordingProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostNotificationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostPhoneProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostProximityProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSettingProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSystemProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostTouchProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostVibrationProfile;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.PhoneProfileConstants.CallState;

import java.util.List;
import java.util.logging.Logger;

/**
 * Host Device Plugin Context.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDevicePlugin extends DevicePluginContext {

    /** サービスID. */
    public static final String SERVICE_ID = "Host";

    /** サービス名. */
    public static final String SERVICE_NAME = "Host";

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("host.dplugin");

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** バッテリー関連の処理と値処理. */
    private HostBatteryManager mHostBatteryManager;

    /** ファイルデータ管理クラス. */
    private FileDataManager mFileDataManager;

    /** メディアプレイヤー管理クラス. */
    private HostMediaPlayerManager mHostMediaPlayerManager;

    /** レコーダ管理クラス. */
    private HostDeviceRecorderManager mRecorderMgr;
    private final BroadcastReceiver mHostConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                onReceivedOutGoingCall(intent);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                    || WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                onChangedWifiStatus();
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)
                    || BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                onChangedBluetoothStatus();
            } else if (PreviewServerProvider.DELETE_PREVIEW_ACTION.equals(action)) {
                stopWebServer(intent);
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public HostDevicePlugin(Context context) {
        super(context);

        // Manager同梱のため、LocalOAuthを無効化
        setUseLocalOAuth(false);

        mFileMgr = new FileManager(context, HostFileProvider.class.getName());
        mFileDataManager = new FileDataManager(mFileMgr);

        mHostBatteryManager = new HostBatteryManager(this);
        mHostBatteryManager.getBatteryInfo();

        mRecorderMgr = new HostDeviceRecorderManager(this);
        mRecorderMgr.createRecorders(mFileMgr);
        mRecorderMgr.start();

        mHostMediaPlayerManager = new HostMediaPlayerManager(this);

        DConnectService hostService = new DConnectService(SERVICE_ID);
        hostService.setName(SERVICE_NAME);
        hostService.setOnline(true);
        hostService.addProfile(new HostBatteryProfile(mHostBatteryManager));
        hostService.addProfile(new HostCanvasProfile());
        hostService.addProfile(new HostConnectionProfile(BluetoothAdapter.getDefaultAdapter()));
        hostService.addProfile(new HostFileProfile(mFileMgr));
        hostService.addProfile(new HostKeyEventProfile());
        hostService.addProfile(new HostMediaPlayerProfile(mHostMediaPlayerManager));
        hostService.addProfile(new HostNotificationProfile());
        hostService.addProfile(new HostPhoneProfile());
        hostService.addProfile(new HostSettingProfile());
        hostService.addProfile(new HostTouchProfile());
        hostService.addProfile(new HostVibrationProfile());

        if (checkSensorHardware()) {
            hostService.addProfile(new HostDeviceOrientationProfile());
        }

        if (checkProximityHardware()) {
            hostService.addProfile(new HostProximityProfile());
        }

        HostDeviceRecorder dRecorder = mRecorderMgr.getRecorder(null);
        if (checkCameraHardware() && dRecorder != null || HostDeviceRecorderManager.isSupportedMediaProjection()) {
            hostService.addProfile(new HostMediaStreamingRecordingProfile(mRecorderMgr, mFileMgr));
            hostService.addProfile(new HostLightProfile(getContext(), mRecorderMgr));
        }

        if (checkLocationHardware()) {
            hostService.addProfile(new HostGeolocationProfile());
        }

        getServiceProvider().addService(hostService);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(PreviewServerProvider.DELETE_PREVIEW_ACTION);
        getContext().registerReceiver(mHostConnectionReceiver, filter);

    }

    @Override
    public void release() {
        mRecorderMgr.stop();
        mRecorderMgr.clean();
        mFileDataManager.stopTimer();
        getContext().unregisterReceiver(mHostConnectionReceiver);
        super.release();
    }

    // Managerアンインストール検知時の処理。
    @Override
    protected void onManagerUninstalled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    // Manager起動通知受信時の処理。
    @Override
    protected void onManagerLaunched() {
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerLaunched");
        }
    }

    // Manager正常終了通知受信時の処理。
    @Override
    protected void onManagerTerminated() {
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    // ManagerのEvent送信経路切断通知受信時の処理。
    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (origin != null) {
            EventManager.INSTANCE.removeEvents(origin);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    // Device Plug-inへのReset要求受信時の処理。
    @Override
    protected void onDevicePluginReset() {
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HostSystemProfile();
    }

    @Override
    protected int getPluginXmlResId() {
        return R.xml.org_deviceconnect_android_deviceplugin_host;
    }
    /**
     * Get a instance of FileManager.
     *
     * @return FileManager
     */
    public FileManager getFileManager() {
        return mFileMgr;
    }

    private void stopWebServer(final Intent intent) {
        mRecorderMgr.stopWebServer(intent.getStringExtra(PreviewServerProvider.EXTRA_CAMERA_ID));
    }

    private void onChangedBluetoothStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(SERVICE_ID, HostConnectionProfile.PROFILE_NAME, null,
                HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
            Bundle bluetoothConnecting = new Bundle();
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            HostConnectionProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, bluetoothConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    private void onChangedWifiStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(SERVICE_ID, HostConnectionProfile.PROFILE_NAME, null,
                HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);
            Bundle wifiConnecting = new Bundle();
            WifiManager wifiMgr = getWifiManager();
            HostConnectionProfile.setEnable(wifiConnecting, wifiMgr.isWifiEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, wifiConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    private void onReceivedOutGoingCall(final Intent intent) {
        List<Event> events = EventManager.INSTANCE.getEventList(SERVICE_ID, HostPhoneProfile.PROFILE_NAME, null,
                HostPhoneProfile.ATTRIBUTE_ON_CONNECT);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostPhoneProfile.setAttribute(mIntent, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);
            Bundle phoneStatus = new Bundle();
            HostPhoneProfile.setPhoneNumber(phoneStatus, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
            HostPhoneProfile.setState(phoneStatus, CallState.START);
            HostPhoneProfile.setPhoneStatus(mIntent, phoneStatus);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    private WifiManager getWifiManager() {
        return (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        // 全イベント削除
        EventManager.INSTANCE.removeAll();

        // バッテリー関連イベントリスナー解除
        mHostBatteryManager.clear();

        // FileDescriptorProfile リセット
        mFileDataManager.clear();

        // KeyEventProfile リセット
        DConnectService service = getServiceProvider().getService(SERVICE_ID);
        HostKeyEventProfile keyEventProfile = (HostKeyEventProfile) service.getProfile(KeyEventProfile.PROFILE_NAME);
        if (keyEventProfile != null) {
            keyEventProfile.resetKeyEventProfile();
        }

        // タッチイベントをリセット
        HostTouchProfile touchProfile = (HostTouchProfile) service.getProfile(TouchProfile.PROFILE_NAME);
        if (touchProfile != null) {
            touchProfile.resetTouchProfile();
        }

        // MediaPlayerProfile 状態変化イベント通知フラグクリア
        mHostMediaPlayerManager.forceStop();

        // MediaStreamingRecorder リセット
        mRecorderMgr.clean();
    }

    /**
     * カメラを端末がサポートしているかチェックします.
     * @return カメラをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkCameraHardware() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 位置情報を端末がサポートしているかチェックします.
     * @return 位置情報をサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkLocationHardware() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    /**
     * 近接センサーを端末がサポートしているかチェックします.
     * @return 近接センサーをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkProximityHardware() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
    }

    /**
     * 加速度センサーを端末がサポートしているかチェックします.
     * @return 加速度センサーをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkSensorHardware() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) ||
                getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
    }

    @Override
    protected String getCertificateAlias() {
        return "org.deviceconnect.android.deviceplugin.host";
    }
}