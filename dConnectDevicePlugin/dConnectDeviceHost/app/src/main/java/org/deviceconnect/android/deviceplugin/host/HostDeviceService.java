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
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperManager;
import org.deviceconnect.android.deviceplugin.host.demo.HostDemoInstaller;
import org.deviceconnect.android.deviceplugin.host.file.FileDataManager;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.HostMediaPlayerManager;
import org.deviceconnect.android.deviceplugin.host.profile.HostBatteryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostCameraProfile;
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
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Host Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostDeviceService extends DConnectMessageService {

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

    /** カメラ管理クラス. */
    private CameraWrapperManager mCameraWrapperManager;

    /** レコーダ管理クラス. */
    private HostDeviceRecorderManager mRecorderMgr;

    /**
     * Phone プロファイルの実装.
     */
    private HostPhoneProfile mPhoneProfile;

    /**
     * MediaStreamRecordingProfile の実装.
     */
    private HostMediaStreamingRecordingProfile mHostMediaStreamRecordingProfile;

    /**
     * デモページインストーラ.
     */
    private DemoInstaller mDemoInstaller;

    /**
     * デモページアップデート通知.
     */
    private DemoInstaller.Notification mDemoNotification;

    /**
     * ブロードキャストレシーバー.
     */
    private final BroadcastReceiver mHostConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            mLogger.info("onReceived: action=" + action);
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                onReceivedOutGoingCall(intent);
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                onReceivedPhoneStateChanged(intent);
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
     * デモページ関連の通知を受信するレシーバー.
     */
    private final BroadcastReceiver mDemoNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            mLogger.info("Demo Notification: " + action);
            mDemoNotification.cancel(context);
            if (DemoInstaller.Notification.ACTON_UPDATE_DEMO.equals(action)) {
                updateDemoPage(context);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Manager同梱のため、LocalOAuthを無効化
        setUseLocalOAuth(false);

        mFileMgr = new FileManager(this, HostFileProvider.class.getName());
        mFileDataManager = new FileDataManager(mFileMgr);

        mDemoInstaller = new HostDemoInstaller(getApplicationContext());
        mDemoNotification = new DemoInstaller.Notification(
                1,
                getString(R.string.app_name_host),
                R.drawable.dconnect_icon,
                "org.deviceconnect.android.deviceconnect.host.channel.demo",
                "Host Plugin Demo Page",
                "Host Plugin Demo Page"
        );

        mHostBatteryManager = new HostBatteryManager(getPluginContext());
        mHostBatteryManager.getBatteryInfo();

        mRecorderMgr = new HostDeviceRecorderManager(getPluginContext());
        initRecorders(mRecorderMgr);
        mRecorderMgr.start();

        mHostMediaPlayerManager = new HostMediaPlayerManager(getPluginContext());

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
        mPhoneProfile = new HostPhoneProfile((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        hostService.addProfile(mPhoneProfile);
        hostService.addProfile(new HostSettingProfile());
        hostService.addProfile(new HostTouchProfile());
        hostService.addProfile(new HostVibrationProfile());

        if (checkSensorHardware()) {
            hostService.addProfile(new HostDeviceOrientationProfile());
        }

        if (checkProximityHardware()) {
            hostService.addProfile(new HostProximityProfile());
        }

        if (mRecorderMgr.getRecorders().length > 0) {
            mHostMediaStreamRecordingProfile = new HostMediaStreamingRecordingProfile(mRecorderMgr, mFileMgr);
            hostService.addProfile(mHostMediaStreamRecordingProfile);
            hostService.addProfile(new HostCameraProfile());
        }
        if (checkCameraHardware()) {
            HostDeviceRecorder defaultRecorder = mRecorderMgr.getRecorder(null);
            if (defaultRecorder instanceof HostDevicePhotoRecorder) {
                hostService.addProfile(new HostLightProfile(this, mRecorderMgr));
            }
        }

        if (checkLocationHardware()) {
            hostService.addProfile(new HostGeolocationProfile());
        }

        getServiceProvider().addService(hostService);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(PreviewServerProvider.DELETE_PREVIEW_ACTION);
        registerReceiver(mHostConnectionReceiver, filter);

        registerDemoNotification();
        updateDemoPageIfNeeded();
    }

    private void registerDemoNotification() {
        IntentFilter filter  = new IntentFilter();
        filter.addAction(DemoInstaller.Notification.ACTON_CONFIRM_NEW_DEMO);
        filter.addAction(DemoInstaller.Notification.ACTON_UPDATE_DEMO);
        registerReceiver(mDemoNotificationReceiver, filter);
    }

    private void initRecorders(final HostDeviceRecorderManager recorderMgr) {
        if (checkCameraHardware()) {
            mCameraWrapperManager = new CameraWrapperManager(this);
            recorderMgr.createCameraRecorders(mCameraWrapperManager, mFileMgr);
        }
        if (checkMicrophone()) {
            recorderMgr.createAudioRecorders();
        }
        if (checkMediaProjection()) {
            recorderMgr.createScreenCastRecorder(mFileMgr);
        }
    }

    private void updateDemoPageIfNeeded() {
        final Context context = getApplicationContext();
        if (DemoInstaller.isUpdateNeeded(context)) {
            mLogger.info("Demo page must be updated.");
            updateDemoPage(context);
        } else {
            mLogger.info("Demo page update is not needed.");
        }
    }

    private void updateDemoPage(final Context context) {
        mDemoInstaller.update(new DemoInstaller.UpdateCallback() {
            @Override
            public void onBeforeUpdate(final File demoDir) {
                mLogger.info("Updating demo page: " + demoDir.getAbsolutePath());
            }

            @Override
            public void onAfterUpdate(final File demoDir) {
                mLogger.info("Updated demo page: " + demoDir.getAbsolutePath());
                mDemoNotification.showUpdateSuccess(context);
            }

            @Override
            public void onFileError(final IOException e) {
                mLogger.severe("Failed to update demo page for file error: " + e.getMessage());
                mDemoNotification.showUpdateError(context);
            }

            @Override
            public void onUnexpectedError(final Throwable e) {
                mLogger.severe("Failed to update demo page for unexpected error: " + e.getMessage());
                mDemoNotification.showUpdateError(context);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onDestroy() {
        mRecorderMgr.stop();
        mRecorderMgr.clean();
        mRecorderMgr.destroy();
        mFileDataManager.stopTimer();
        if (mCameraWrapperManager != null) {
            mCameraWrapperManager.destroy();
        }
        if (mHostMediaStreamRecordingProfile != null) {
            mHostMediaStreamRecordingProfile.destroy();
        }
        unregisterReceiver(mHostConnectionReceiver);
        unregisterReceiver(mDemoNotificationReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (PreviewServerProvider.DELETE_PREVIEW_ACTION.equals(action)) {
            return stopWebServer(intent);
        }
        return super.onStartCommand(intent, flags, startId);
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

    /**
     * Get a instance of FileManager.
     *
     * @return FileManager
     */
    public FileManager getFileManager() {
        return mFileMgr;
    }

    /**
     * カメラ管理クラスを取得する.
     *
     * @return カメラ管理クラス. 端末がカメラを持たない場合は<code>null</code>
     */
    public CameraWrapperManager getCameraManager() {
        return mCameraWrapperManager;
    }

    /**
     * レコーダー管理クラスを取得する.
     *
     * @return レコーダー管理クラス
     */
    public HostDeviceRecorderManager getRecorderManager() {
        return mRecorderMgr;
    }

    private int stopWebServer(final Intent intent) {
        mRecorderMgr.stopWebServer(intent.getStringExtra(PreviewServerProvider.EXTRA_CAMERA_ID));
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        mLogger.info("onConfigurationChanged: rotation=" + rotation);
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
        mPhoneProfile.onNewOutGoingCall(intent);
    }

    private void onReceivedPhoneStateChanged(final Intent intent) {
        mPhoneProfile.onPhoneStateChanged(intent);
    }

    private WifiManager getWifiManager() {
        return (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        if (mRecorderMgr != null) {
            mRecorderMgr.clean();
        }
    }

    /**
     * カメラを端末がサポートしているかチェックします.
     * @return カメラをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkCameraHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 位置情報を端末がサポートしているかチェックします.
     * @return 位置情報をサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkLocationHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    /**
     * 近接センサーを端末がサポートしているかチェックします.
     * @return 近接センサーをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkProximityHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
    }

    /**
     * 加速度センサーを端末がサポートしているかチェックします.
     * @return 加速度センサーをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkSensorHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) ||
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
    }

    /**
     * マイク入力を端末がサポートしているかチェックします.
     * @return マイク入力をサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkMicrophone() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    /**
     * MediaProjection APIを端末がサポートしているかチェックします.
     * @return MediaProjection APIをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkMediaProjection() {
        return HostDeviceRecorderManager.isSupportedMediaProjection();
    }

    @Override
    protected String getCertificateAlias() {
        return "org.deviceconnect.android.deviceplugin.host";
    }
}