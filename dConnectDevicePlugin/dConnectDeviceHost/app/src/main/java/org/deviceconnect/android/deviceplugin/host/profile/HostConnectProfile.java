/*
 HostConnectProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.activity.BluetoothManageActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.util.Log;

/**
 * Connect プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostConnectProfile extends ConnectProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    /** Bluetooth Adapter. */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * コンストラクタ.
     * 
     * @param bluetoothAdapter Bluetoothアダプタ.
     */
    public HostConnectProfile(final BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    @Override
    protected boolean onGetWifi(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            getEnabledOfWiFi(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;

    }

    @Override
    protected boolean onGetBluetooth(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            getEnabledBluetooth(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;

    }

    @Override
    protected boolean onGetBLE(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            getEnabledOfBluetoothLowEnery(request, response);
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;

    }

    @Override
    protected boolean onGetNFC(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.getContext());
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
        }

        return true;

    }

    @Override
    protected boolean onPutWifi(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setEnabledOfWiFi(request, response, true);
        }
        return true;
    }

    @Override
    protected boolean onPutBluetooth(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setEnabledBluetooth(request, response, true);
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutBLE(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {

            setEnabledBluetooth(request, response, true);
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteWifi(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setEnabledOfWiFi(request, response, false);
        }
        return true;
    }

    @Override
    protected boolean onDeleteBluetooth(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setEnabledBluetooth(request, response, false);
        }
        return true;
    }

    @Override
    protected boolean onDeleteBLE(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setEnabledBluetooth(request, response, false);
        }
        return true;
    }

    @Override
    protected boolean onDeleteNFC(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setEnabledNfc(request, response, false);
        }
        return true;
    }

    @Override
    protected boolean onPutOnWifiChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HostDeviceService) getContext()).setServiceId(serviceId);
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }

        }

        return true;
    }

    @Override
    protected boolean onPutOnBluetoothChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HostDeviceService) getContext()).setServiceId(serviceId);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }

        return true;
    }

    @Override
    protected boolean onDeleteOnWifiChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {

            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                MessageUtils.setError(response, 100, "Can not unregister event.");
                return true;
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnBluetoothChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {

            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                MessageUtils.setError(response, 100, "Can not unregister event.");
                return true;
            }
        }
        return true;
    }

    /**
     * WiFi接続の状態を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void getEnabledOfWiFi(final Intent request, final Intent response) {

        setResult(response, IntentDConnectMessage.RESULT_OK);

        WifiManager mWifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
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
        WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
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
                getContext().startActivity(intent);
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

    /**
     * NFCの状態を変更する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled WiFi接続状態
     */
    protected void setEnabledNfc(final Intent request, final Intent response, final boolean enabled) {
        MessageUtils.setNotSupportActionError(response, "not support change status of nfc.");
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);

        return match.find();
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

}
