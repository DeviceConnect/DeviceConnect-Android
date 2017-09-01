/*
 HOGPMessageService.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPHogpProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPSystemProfile;
import org.deviceconnect.android.deviceplugin.hogp.server.AbstractHOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * HOGPプラグインの処理を行う.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPMessageService extends DConnectMessageService {

    /**
     * サービス名.
     */
    public static final String HOGP_NAME = "HOGP";

    /**
     * サービスID.
     */
    public static final String HOGP_SERVICE_ID = "hogp";

    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "HOGP";

    /**
     * HOGPサーバ.
     */
    private AbstractHOGPServer mHOGPServer;

    /**
     * HOGPプラグインの設定.
     */
    private HOGPSetting mHOGPSetting;

    /**
     * Bluetoothの状態通知を受け取るレシーバー.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (DEBUG) {
                            Log.i(TAG, "Bluetooth is off.");
                        }
                        stopHOGPServer();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        if (DEBUG) {
                            Log.i(TAG, "Bluetooth is on.");
                        }
                        if (mHOGPSetting.isEnabledServer()) {
                            try {
                                startHOGPServer();
                            } catch (Exception e) {
                                if (DEBUG) {
                                    Log.w(TAG, "Failed to start HOGPServer.", e);
                                }
                            }
                        }
                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                switch (state) {
                    case BluetoothDevice.BOND_BONDING:
                        if (DEBUG) {
                            Log.d(TAG, "Bond bonding.");
                        }
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        if (DEBUG) {
                            Log.d(TAG, "Bond bonded.");
                        }
                        break;

                    default:
                        if (DEBUG) {
                            Log.d(TAG, "Bond error.");
                        }
                        try {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (device != null) {
                                getServiceProvider().removeService(device.getAddress());
                            }
                        } catch (Exception e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to remove device.", e);
                            }
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mHOGPSetting = new HOGPSetting(getApplicationContext());

        DConnectService service = new DConnectService(HOGP_SERVICE_ID);
        service.addProfile(new HOGPHogpProfile());
        service.setName(HOGP_NAME);
        service.setOnline(true);
        getServiceProvider().addService(service);

        if (mHOGPSetting.isEnabledServer()) {
            try {
                startHOGPServer();
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to start HOGPServer.", e);
                }
            }
        }

        setUseLocalOAuth(mHOGPSetting.isEnabledOAuth());

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        stopHOGPServer();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HOGPSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // TODO アプリとのWebSocket接続が切断された時に実行したい処理. 実装は任意.
        // イベントのAPIがないので特に処理は行わない
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO Device Connect Managerの設定画面上で「プラグイン再起動」を要求された場合の処理. 実装は任意.
    }

    /**
     * HOGPの設定を取得します.
     * @return HOGP設定
     */
    public HOGPSetting getHOGPSetting() {
        return mHOGPSetting;
    }

    /**
     * Local OAuthの有効・無効を設定します.
     * @param flag trueの場合はLocal OAuthを有効、falseの場合は無効
     */
    public void setEnabledOAuth(final boolean flag) {
        setUseLocalOAuth(flag);
        mHOGPSetting.setEnabledOAuth(flag);
    }

    /**
     * HOGPサーバを取得します.
     * <p>
     * HOGPサーバが開始されていない場合にはnullを返却します。
     * </p>
     * @return HOGPサーバ
     */
    public synchronized AbstractHOGPServer getHOGPServer() {
        return mHOGPServer;
    }

    /**
     * HOGPサーバを起動します.
     */
    public synchronized void startHOGPServer() {
        if (mHOGPServer != null) {
            if (DEBUG) {
                Log.d(TAG, "HOGP Server is already running.");
            }
            return;
        }

        if (DEBUG) {
            Log.i(TAG, "Start the HOGP Server.");
            Log.i(TAG, "Mouse Mode: " + mHOGPSetting.getMouseMode());
            Log.i(TAG, "Keyboard: " + mHOGPSetting.isEnabledKeyboard());
            Log.i(TAG, "Joystick: " + mHOGPSetting.isEnabledJoystick());
        }

        if (mHOGPSetting.getMouseMode() == HOGPServer.MouseMode.NONE && !mHOGPSetting.isEnabledKeyboard()) {
            throw new RuntimeException("The feature is not set. Please set mouse or keyboard.");
        }

        mHOGPServer = new HOGPServer(this, mHOGPSetting.getMouseMode(), mHOGPSetting.isEnabledKeyboard(), false);
        mHOGPServer.setOnHOGPServerListener(new AbstractHOGPServer.OnHOGPServerListener() {
            @Override
            public void onConnected(final BluetoothDevice device) {
                if (DEBUG) {
                    Log.d(TAG, "Connected the device. " + device.getName());
                }

                DConnectService service = new HOGPService(device, mHOGPSetting);
                service.setOnline(true);

                getServiceProvider().removeService(device.getAddress());
                getServiceProvider().addService(service);
            }

            @Override
            public void onDisconnected(final BluetoothDevice device) {
                if (DEBUG) {
                    Log.d(TAG, "Disconnected the device. " + device.getName());
                }

                DConnectService service = getServiceProvider().getService(device.getAddress());
                if (service != null) {
                    service.setOnline(false);
                }
            }
        });

        PackageManager pm = getPackageManager();
        String appName = "";
        int versionCode = 0;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
        mHOGPServer.setManufacturerName(getPackageName());
        mHOGPServer.setDeviceName(appName);
        mHOGPServer.setSerialNumber("" + versionCode);
        mHOGPServer.setDataSendingRate(10);
        mHOGPServer.start();
    }

    /**
     * HOGPサーバを停止します.
     */
    public synchronized void stopHOGPServer() {
        if (mHOGPServer != null) {
            if (DEBUG) {
                Log.i(TAG, "Stop the HOGP Server.");
            }

            for (DConnectService s : getServiceProvider().getServiceList()) {
                if (s instanceof HOGPService) {
                    s.setOnline(false);
                }
            }

            mHOGPServer.stop();
            mHOGPServer = null;
        }
    }
}