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
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.demo.HostDemoInstaller;
import org.deviceconnect.android.deviceplugin.host.demo.HostDemoManager;
import org.deviceconnect.android.deviceplugin.host.file.FileDataManager;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.HostMediaPlayerManager;
import org.deviceconnect.android.deviceplugin.host.phone.HostPhoneManager;
import org.deviceconnect.android.deviceplugin.host.profile.HostBatteryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostCameraProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostCanvasProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostConnectionProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostGeolocationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostLiveStreamingProfile;
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
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

/**
 * Host Device Plugin Context.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDevicePlugin extends DConnectMessageService {

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
    private HostMediaRecorderManager mRecorderMgr;

    /**
     * 接続管理クラス.
     */
    private HostConnectionManager mHostConnectionManager;

    /**
     * 電話管理クラス.
     */
    private HostPhoneManager mHostPhoneManager;

    /**
     * デモ管理クラス.
     */
    private HostDemoManager mHostDemoManager;

    /**
     * SSL コンテキスト.
     */
    private SSLContext mSSLContext;

    @Override
    public void onCreate() {
        super.onCreate();

        // Manager 同梱のため、LocalOAuthを無効化に設定
        setUseLocalOAuth(false);

        mFileMgr = new FileManager(this, HostFileProvider.class.getName());
        mFileDataManager = new FileDataManager(mFileMgr);

        SRT.startup();

        DConnectService hostService = new DConnectService(SERVICE_ID);
        hostService.setName(SERVICE_NAME);
        hostService.setOnline(true);

        mRecorderMgr = new HostMediaRecorderManager(getPluginContext(), mFileMgr);
        mRecorderMgr.initRecorders();
        mRecorderMgr.start();
        //  MediaRecorder が存在する場合には、MediaStreamRecording と Camera プロファイルを追加
        if (mRecorderMgr.getRecorders().length > 0) {
            hostService.addProfile(new HostMediaStreamingRecordingProfile(mRecorderMgr, mFileMgr));
            hostService.addProfile(new HostCameraProfile(mRecorderMgr));
        }

        mHostMediaPlayerManager = new HostMediaPlayerManager(getPluginContext());
        hostService.addProfile(new HostMediaPlayerProfile(mHostMediaPlayerManager));

        mHostBatteryManager = new HostBatteryManager(getPluginContext());
        mHostBatteryManager.getBatteryInfo();
        hostService.addProfile(new HostBatteryProfile(mHostBatteryManager));

        hostService.addProfile(new HostCanvasProfile());

        mHostConnectionManager = new HostConnectionManager(getPluginContext());
        hostService.addProfile(new HostConnectionProfile(mHostConnectionManager));

        hostService.addProfile(new HostFileProfile(mFileMgr));
        hostService.addProfile(new HostKeyEventProfile());
        hostService.addProfile(new HostNotificationProfile());

        mHostPhoneManager = new HostPhoneManager(getPluginContext());
        hostService.addProfile(new HostPhoneProfile(mHostPhoneManager));

        hostService.addProfile(new HostSettingProfile());
        hostService.addProfile(new HostTouchProfile());
        hostService.addProfile(new HostVibrationProfile());

        if (checkSensorHardware()) {
            hostService.addProfile(new HostDeviceOrientationProfile());
        }

        if (checkProximityHardware()) {
            hostService.addProfile(new HostProximityProfile());
        }

        // カメラが使用できる場合は、Light プロファイルを追加
        if (checkCameraHardware()) {
            HostMediaRecorder defaultRecorder = mRecorderMgr.getRecorder(null);
            if (defaultRecorder instanceof HostDevicePhotoRecorder) {
                hostService.addProfile(new HostLightProfile(this, mRecorderMgr));
            }
            hostService.addProfile(new HostLiveStreamingProfile(mRecorderMgr));
        }

        if (checkLocationHardware()) {
            hostService.addProfile(new HostGeolocationProfile());
        }

        getServiceProvider().addService(hostService);

        mHostDemoManager = new HostDemoManager(this);
    }

    @Override
    public void onDestroy() {
        mRecorderMgr.destroy();
        SRT.cleanup();

        mFileDataManager.stopTimer();

        if (mHostConnectionManager != null) {
            mHostConnectionManager.destroy();
            mHostConnectionManager = null;
        }

        if (mHostPhoneManager != null) {
            mHostPhoneManager.destroy();
            mHostPhoneManager = null;
        }

        if (mHostDemoManager != null) {
            mHostDemoManager.destroy();
            mHostDemoManager = null;
        }

        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HostSystemProfile();
    }

    @Override
    protected String getCertificateAlias() {
        return "org.deviceconnect.android.deviceplugin.host";
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
    protected void onDevicePluginEnabled() {
    }

    @Override
    protected void onDevicePluginDisabled() {
    }

    public HostMediaRecorderManager getHostMediaRecorderManager() {
        return mRecorderMgr;
    }

    // SSL

    /**
     * SSLContext を提供するインターフェース.
     */
    public interface SSLContextCallback {
        void onGet(SSLContext context);
        void onError();
    }

    public void getSSLContext(final SSLContextCallback callback) {
        final SSLContext sslContext = mSSLContext;
        if (sslContext != null) {
            mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Already created SSL Context: " + sslContext);
            callback.onGet(sslContext);
        } else {
            requestKeyStore(NetworkUtil.getIPAddress(this), new KeyStoreCallback() {
                public void onSuccess(final KeyStore keyStore, final Certificate certificate, final Certificate certificate1) {
                    try {
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Creating SSL Context...");
                        mSSLContext = createSSLContext(keyStore, "0000");
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Created SSL Context: " + mSSLContext);
                        callback.onGet(mSSLContext);
                    } catch (GeneralSecurityException e) {
                        mLogger.log(Level.WARNING, "getSSLContext: requestKeyStore: onSuccess: Failed to create SSL Context", e);
                        callback.onError();
                    }
                }

                public void onError(final KeyStoreError keyStoreError) {
                    mLogger.warning("getSSLContext: requestKeyStore: onError: error = " + keyStoreError);
                    callback.onError();
                }
            });
        }
    }

    @Override
    protected boolean usesAutoCertificateRequest() {
        return true;
    }

    @Override
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
        try {
            if (keyStore == null) {
                return;
            }
            mSSLContext = createSSLContext(keyStore, "0000");
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to update keystore", e);
        }
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
        return HostMediaRecorderManager.isSupportedMediaProjection();
    }
}
