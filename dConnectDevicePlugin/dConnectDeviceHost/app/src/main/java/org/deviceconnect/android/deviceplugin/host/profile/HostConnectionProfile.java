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
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ConnectionProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

/**
 * Connection プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostConnectionProfile extends ConnectionProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    private HostConnectionManager mHostConnectionManager;

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
     * @param manager 接続管理クラス.
     */
    public HostConnectionProfile(HostConnectionManager manager) {
        mHostConnectionManager = manager;
        mHostConnectionManager.setHostConnectionEventListener(new HostConnectionManager.ConnectionEventListener() {
            @Override
            public void onChangedWifiStatus() {
                postOnChangedWifiStatus();
            }

            @Override
            public void onChangedBluetoothStatus() {
                postOnChangedBluetoothStatus();
            }
        });

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
    private void getEnabledOfWiFi(final Intent request, final Intent response) {
        setResult(response, IntentDConnectMessage.RESULT_OK);
        response.putExtra(PARAM_ENABLE, mHostConnectionManager.isWifiEnabled());
    }

    /**
     * WiFi接続の状態を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled WiFi接続状態
     */
    private void setEnabledOfWiFi(final Intent request, final Intent response, final boolean enabled) {
        if (mHostConnectionManager.setWifiEnabled(enabled)) {
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
    private void getEnabledBluetooth(final Intent request, final Intent response) {
        setResult(response, IntentDConnectMessage.RESULT_OK);
        response.putExtra(PARAM_ENABLE, mHostConnectionManager.isBluetoothEnabled());
    }

    /**
     * Bluetooth接続の状態を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled Bluetooth接続状態
     */
    private void setEnabledBluetooth(final Intent request, final Intent response, final boolean enabled) {
        boolean result = mHostConnectionManager.setBluetoothEnabled(enabled);
        if (result) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
        } else {
            setResult(response, IntentDConnectMessage.RESULT_ERROR);
        }
    }

    /**
     * Bluetooth Low Enery接続の状態を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void getEnabledOfBluetoothLowEnery(final Intent request, final Intent response) {
        response.putExtra(PARAM_ENABLE, mHostConnectionManager.getEnabledOfBluetoothLowEnergy());
    }

    private void postOnChangedBluetoothStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostConnectionProfile.PROFILE_NAME, null, HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
            Bundle bluetoothConnecting = new Bundle();
            HostConnectionProfile.setEnable(bluetoothConnecting, mHostConnectionManager.isBluetoothEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, bluetoothConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    private void postOnChangedWifiStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostConnectionProfile.PROFILE_NAME, null, HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);
            Bundle wifiConnecting = new Bundle();
            HostConnectionProfile.setEnable(wifiConnecting, mHostConnectionManager.isWifiEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, wifiConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }
}
