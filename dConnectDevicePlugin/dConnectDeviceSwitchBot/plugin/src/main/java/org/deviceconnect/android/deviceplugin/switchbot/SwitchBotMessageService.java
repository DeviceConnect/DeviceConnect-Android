/*
 SwitchBotMessageService.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.switchbot.demo.SwitchBotDemoInstaller;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDeviceProvider;
import org.deviceconnect.android.deviceplugin.switchbot.settings.Settings;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotButtonProfile;
import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotSwitchProfile;
import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotSystemProfile;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SwitchBotMessageService extends DConnectMessageService implements SwitchBotDevice.EventListener {
    private static final String TAG = "SwitchBotMessageService";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String DEMO_INSTALLER_NOTIFICATION_CHANNEL_ID = "org.deviceconnect.android.deviceplugin.switchbot.notification";
    private static final String DEMO_INSTALLER_NOTIFICATION_CHANNEL_TITLE = "SwitchBot Plugin Demo";
    private static final String DEMO_INSTALLER_NOTIFICATION_CHANNEL_DESCRIPTION = "SwitchBot Plugin Demo";
    private ArrayList<SwitchBotDevice> mSwitchBotDevices;
    private SwitchBotDeviceProvider mSwitchBotDeviceProvider;
    private SwitchBotDemoInstaller mSwitchBotDemoInstaller;
    private DemoInstaller.Notification mSwitchBotDemoInstallerNotification;
    private BroadcastReceiver mSwitchBotDemoNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        mSwitchBotDeviceProvider = new SwitchBotDeviceProvider(this, this);
        mSwitchBotDevices = mSwitchBotDeviceProvider.getDevices();
        for (SwitchBotDevice switchBotDevice : mSwitchBotDevices) {
            createService(switchBotDevice);
        }

        boolean localOAuth = Settings.getBoolean(this, Settings.KEY_LOCAL_OAUTH, true);
        if (DEBUG) {
            Log.d(TAG, "localOAuth : " + localOAuth);
        }
        setUseLocalOAuth(localOAuth);

        mSwitchBotDemoInstaller = new SwitchBotDemoInstaller(this);
        mSwitchBotDemoInstallerNotification = new DemoInstaller.Notification(
                1, getString(R.string.app_name), R.drawable.ic_launcher,
                DEMO_INSTALLER_NOTIFICATION_CHANNEL_ID, DEMO_INSTALLER_NOTIFICATION_CHANNEL_TITLE,
                DEMO_INSTALLER_NOTIFICATION_CHANNEL_DESCRIPTION);

        registerDemoNotification();

        updateDemoPage();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SwitchBotSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
        for (SwitchBotDevice switchBotDevice : mSwitchBotDevices) {
            switchBotDevice.disconnect();
        }
        unregisterDemoNotification();
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // TODO アプリとのWebSocket接続が切断された時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO Device Connect Managerの設定画面上で「プラグイン再起動」を要求された場合の処理. 実装は任意.
    }

    /**
     *
     */
    public void setLocalOAuthPreference(boolean value) {
        if (DEBUG) {
            Log.d(TAG, "setLocalOAuthPreference()");
            Log.d(TAG, "value : " + value);
        }
        setUseLocalOAuth(value);
    }

    /**
     * デバイス登録処理
     *
     * @param switchBotDevice 対象デバイス
     * @return true : 登録成功, false : 登録失敗(デバイス名の重複)
     */
    public Boolean registerDevice(SwitchBotDevice switchBotDevice) {
        if (mSwitchBotDeviceProvider.insert(switchBotDevice)) {
            mSwitchBotDevices.add(switchBotDevice);
            createService(switchBotDevice);
            return true;
        } else {
            return false;
        }
    }

    /**
     * デバイス登録解除処理
     *
     * @param switchBotDevices 対象デバイス
     */
    public void unregisterDevices(ArrayList<SwitchBotDevice> switchBotDevices) {
        if (DEBUG) {
            Log.d(TAG, "unregisterDevice()");
        }
        for (SwitchBotDevice switchBotDevice : switchBotDevices) {
            if (DEBUG) {
                Log.d(TAG, "switchBotDevice : " + switchBotDevice);
                Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
                Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
                Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
            }
            getServiceProvider().removeService(makeServiceId(switchBotDevice));
            switchBotDevice.disconnect();
            removeList(switchBotDevice);
        }
        mSwitchBotDeviceProvider.delete(switchBotDevices);
    }

    /**
     * デバイスリスト削除処理
     *
     * @param switchBotDevice 削除対象デバイス
     */
    private void removeList(SwitchBotDevice switchBotDevice) {
        for (int i = 0; i < mSwitchBotDevices.size(); i++) {
            if (mSwitchBotDevices.get(i).getDeviceName().equals(switchBotDevice.getDeviceName())) {
                mSwitchBotDevices.remove(i);
                break;
            }
        }
    }

    /**
     * デバイス毎のサービス生成処理
     *
     * @param switchBotDevice 対象デバイス
     */
    public void createService(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "createService()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        DConnectService service = new DConnectService(makeServiceId(switchBotDevice));
        // TODO サービス名の設定
        service.setName(makeServiceName(switchBotDevice));
        // TODO サービスの使用可能フラグのハンドリング
        //service.setOnline(true);
        // TODO ネットワークタイプの指定 (例: BLE, Wi-Fi)
        service.setNetworkType(NetworkType.UNKNOWN);
        service.addProfile(new SwitchBotButtonProfile(this, switchBotDevice));
        service.addProfile(new SwitchBotSwitchProfile(this, switchBotDevice));
        getServiceProvider().addService(service);
    }

    /**
     * サービスID生成処理
     *
     * @param switchBotDevice 対象デバイス
     * @return サービスID
     */
    private String makeServiceId(final SwitchBotDevice switchBotDevice) {
        return String.format(Locale.US, "SwitchBot.Device.%s", switchBotDevice.getDeviceAddress());
    }

    /**
     * サービス名生成処理
     *
     * @param switchBotDevice 対象デバイス
     * @return サービス名
     */
    private String makeServiceName(final SwitchBotDevice switchBotDevice) {
        return String.format(Locale.US, "%s(SwitchBotDevice)", switchBotDevice.getDeviceName());
    }

    /**
     * デバイス一覧取得処理
     *
     * @return デバイス一覧
     */
    public ArrayList<SwitchBotDevice> getDeviceList() {
        return mSwitchBotDevices;
    }

    /**
     * デバイス取得処理
     *
     * @param deviceName 対象のデバイス名
     * @return デバイス or null
     */
    public SwitchBotDevice getSwitchBotDeviceFromDeviceName(String deviceName) {
        if (DEBUG) {
            Log.d(TAG, "getSwitchBotDeviceFromDeviceName()");
            Log.d(TAG, "deviceName : " + deviceName);
        }
        for (SwitchBotDevice switchBotDevice : mSwitchBotDevices) {
            if (switchBotDevice.getDeviceName().equals(deviceName)) {
                return switchBotDevice;
            }
        }
        return null;
    }

    /**
     * デバイス情報更新処理
     *
     * @param oldDevice 旧デバイス情報
     * @param newDevice 新デバイス情報
     * @return 処理結果。true:成功, false:失敗
     */
    public Boolean modifyDevice(SwitchBotDevice oldDevice, SwitchBotDevice newDevice) {
        if (DEBUG) {
            Log.d(TAG, "modifyDevice()");
            Log.d(TAG, "device name(old) : " + oldDevice.getDeviceName());
            Log.d(TAG, "device name(new) : " + newDevice.getDeviceName());
            Log.d(TAG, "device mode(old) : " + oldDevice.getDeviceMode());
            Log.d(TAG, "device mode(new) : " + newDevice.getDeviceMode());
        }
        if (mSwitchBotDeviceProvider.update(oldDevice, newDevice)) {
            removeList(oldDevice);
            getServiceProvider().removeService(makeServiceName(oldDevice));
            mSwitchBotDevices.add(newDevice);
            createService(newDevice);
            oldDevice.disconnect();
            return true;
        }
        return false;
    }

    /**
     * 接続通知
     *
     * @param switchBotDevice 接続したデバイス
     */
    @Override
    public void onConnect(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onConnect()");
            Log.d(TAG, "switchBotDevice : " + switchBotDevice);
        }
        DConnectService service = getServiceProvider().getService(makeServiceId(switchBotDevice));
        if (service != null) {
            service.setOnline(true);
        }
    }

    /**
     * 切断通知
     *
     * @param switchBotDevice 切断したデバイス
     */
    @Override
    public void onDisconnect(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onDisconnect()");
            Log.d(TAG, "switchBotDevice : " + switchBotDevice);
        }
        DConnectService service = getServiceProvider().getService(makeServiceId(switchBotDevice));
        if (service != null) {
            service.setOnline(false);
        }
    }

    private void registerDemoNotification() {
        IntentFilter filter  = new IntentFilter();
        filter.addAction(DemoInstaller.Notification.ACTON_CONFIRM_NEW_DEMO);
        filter.addAction(DemoInstaller.Notification.ACTON_UPDATE_DEMO);
        registerReceiver(mSwitchBotDemoNotificationReceiver, filter);
    }

    private void unregisterDemoNotification() {
        unregisterReceiver(mSwitchBotDemoNotificationReceiver);
    }

    private void updateDemoPage() {
        final Context context = this;
        if (mSwitchBotDemoInstaller.isUpdateNeeded()) {
            mSwitchBotDemoInstaller.update(new DemoInstaller.UpdateCallback() {
                @Override
                public void onBeforeUpdate(final File demoDir) {
                    // 自動更新を実行する直前
                }

                @Override
                public void onAfterUpdate(final File demoDir) {
                    // 自動更新に成功した直後
                    mSwitchBotDemoInstallerNotification.showUpdateSuccess(context);
                }

                @Override
                public void onFileError(final IOException e) {
                    // 自動更新時にファイルアクセスエラーが発生した場合
                    mSwitchBotDemoInstallerNotification.showUpdateError(context);
                }

                @Override
                public void onUnexpectedError(final Throwable e) {
                    // 自動更新時に不明なエラーが発生した場合
                    mSwitchBotDemoInstallerNotification.showUpdateError(context);
                }
            }, new Handler(Looper.getMainLooper()));
        }
    }
}