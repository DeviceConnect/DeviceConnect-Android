/*
 HostConnectionProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.BluetoothManageActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ConnectionProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Connection プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostConnectionProfile extends ConnectionProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    /** Bluetooth Adapter. */
    private BluetoothAdapter mBluetoothAdapter;
    /** Notification Id */
    private final int NOTIFICATION_ID = 3527;

    private final DConnectApi mGetWifiApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getEnabledOfWiFi(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetBluetoothApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getEnabledBluetooth(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetBleApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getEnabledOfBluetoothLowEnery(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetNfcApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_NFC;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getContext());
            if (adapter != null) {
                if (adapter.isEnabled()) {
                    response.putExtra(PARAM_ENABLE, true);
                } else {
                    response.putExtra(PARAM_ENABLE, false);
                }
            } else {
                response.putExtra(PARAM_ENABLE, false);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutWifiApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledOfWiFi(request, response, true);
            return true;
        }
    };

    private final DConnectApi mPutBluetoothApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, true);
            return true;
        }
    };

    private final DConnectApi mPutBleApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, true);
            return true;
        }
    };

    private final DConnectApi mDeleteWifiApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledOfWiFi(request, response, false);
            return true;
        }
    };

    private final DConnectApi mDeleteBluetoothApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, false);
            return true;
        }
    };

    private final DConnectApi mDeleteBleApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, false);
            return true;
        }
    };

    private final DConnectApi mPutOnWifiChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WIFI_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            setResult(response, DConnectMessage.RESULT_OK);
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }
        }
    };

    private final DConnectApi mPutOnBluetoothChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BLUETOOTH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnWifiChange = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WIFI_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
                return true;
            }
        }
    };

    private final DConnectApi mDeleteOnBluetoothChange = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BLUETOOTH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
                return true;
            }
        }
    };

    /**
     * コンストラクタ.
     * 
     * @param bluetoothAdapter Bluetoothアダプタ.
     */
    public HostConnectionProfile(final BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        addApi(mGetWifiApi);
        addApi(mGetBluetoothApi);
        addApi(mGetBleApi);
        addApi(mGetNfcApi);
        addApi(mPutBluetoothApi);
        addApi(mPutBleApi);
        addApi(mDeleteBluetoothApi);
        addApi(mDeleteBleApi);
        addApi(mPutOnWifiChangeApi);
        addApi(mPutOnBluetoothChangeApi);
        addApi(mDeleteOnWifiChange);
        addApi(mDeleteOnBluetoothChange);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            addApi(mPutWifiApi);
            addApi(mDeleteWifiApi);
        }
    }

    /**
     * WiFi接続の状態を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void getEnabledOfWiFi(final Intent request, final Intent response) {

        setResult(response, IntentDConnectMessage.RESULT_OK);

        WifiManager mWifiManager = getWifiManager();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "WifiManager:" + mWifiManager.isWifiEnabled());
        }
        response.putExtra(PARAM_ENABLE, mWifiManager.isWifiEnabled());
    }

    /**
     * WiFi接続の状態を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled WiFi接続状態
     */
    protected void setEnabledOfWiFi(final Intent request, final Intent response, final boolean enabled) {
        WifiManager wifiMgr = getWifiManager();
        if (wifiMgr.setWifiEnabled(enabled)) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
        } else {
            String msg;
            if (enabled) {
                msg = "Failed to enable WiFi.";
            } else {
                msg = "Failed to disable WiFi.";
            }
            MessageUtils.setUnknownError(response, msg);
        }
    }

    /**
     * Bluetooth接続の状態を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void getEnabledBluetooth(final Intent request, final Intent response) {

        setResult(response, IntentDConnectMessage.RESULT_OK);
        response.putExtra(PARAM_ENABLE, mBluetoothAdapter.isEnabled());
    }

    /**
     * Bluetooth接続の状態を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled Bluetooth接続状態
     */
    protected void setEnabledBluetooth(final Intent request, final Intent response, final boolean enabled) {
        if (enabled) {
            // enable bluetooth
            if (!mBluetoothAdapter.isEnabled()) {

                Intent intent = new Intent(request);
                intent.setClass(getContext(), BluetoothManageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    this.getContext().startActivity(intent);
                } else {
                    NotificationUtils.createNotificationChannel(getContext());
                    NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                            getContext().getString(R.string.host_notification_connection_warnning));
                }

                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                // bluetooth has already enabled
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }

        } else {
            // disable bluetooth
            boolean result = mBluetoothAdapter.disable();

            // create response
            if (result) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                setResult(response, IntentDConnectMessage.RESULT_ERROR);
            }

        }
    }

    /**
     * Bluetooth Low Enery接続の状態を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void getEnabledOfBluetoothLowEnery(final Intent request, final Intent response) {

        // Bluetoothが機能していないときはBluetooth LEも機能しない扱いに。
        if (this.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && mBluetoothAdapter.isEnabled()) {
            response.putExtra(PARAM_ENABLE, true);
        } else {
            response.putExtra(PARAM_ENABLE, false);
        }
    }

    private WifiManager getWifiManager() {
        return (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
}
