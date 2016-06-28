/*
 ConnectProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.ConnectProfileConstants;

/**
 * Connect プロファイル.
 * 
 * <p>
 * スマートデバイスとのネットワーク接続情報を提供するAPI.<br>
 * ネットワーク接続情報を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * AndroidManifest.xmlに追加する必要の有るパーミッション： wifi: ACCESS_WIFI_STATE,
 * CHANGE_WIFI_STATE bluetooth: BLUETOOTH, BLUETOOTH_ADMIN nfc: NFC
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Connect Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>WiFi Connect API [GET] :
 * {@link ConnectProfile#onGetWifi(Intent, Intent, String)}</li>
 * <li>WiFi Connect API [PUT] :
 * {@link ConnectProfile#onPutWifi(Intent, Intent, String)}</li>
 * <li>WiFi Connect API [DELETE] :
 * {@link ConnectProfile#onDeleteWifi(Intent, Intent, String)}</li>
 * <li>WiFi Connect Status Change Event API [Register] :
 * {@link ConnectProfile#onPutOnWifiChange(Intent, Intent, String, String)}</li>
 * <li>WiFi Connect Status Change Event API [Unregister] :
 * {@link ConnectProfile#onDeleteOnWifiChange(Intent, Intent, String, String)}</li>
 * <li>Bluetooth Connect API [GET] :
 * {@link ConnectProfile#onGetBluetooth(Intent, Intent, String)}</li>
 * <li>Bluetooth Connect API [PUT] :
 * {@link ConnectProfile#onPutBluetooth(Intent, Intent, String)}</li>
 * <li>Bluetooth Connect API [DELETE] :
 * {@link ConnectProfile#onDeleteBluetooth(Intent, Intent, String)}</li>
 * <li>Bluetooth Connect Status Change Event API [Register] :
 * {@link ConnectProfile#onPutOnBluetoothChange(Intent, Intent, String, String)
 * )}</li>
 * <li>Bluetooth Connect Status Change Event API [Unregister] :
 * {@link ConnectProfile#onDeleteOnBluetoothChange(Intent, Intent, String, String)
 * )}</li>
 * <li>Bluetooth Discoverable Status API [PUT] :
 * {@link ConnectProfile#onPutBluetoothDiscoverable(Intent, Intent, String)}</li>
 * <li>Bluetooth Discoverable Status API [DELETE] :
 * {@link ConnectProfile#onDeleteBluetoothDiscoverable(Intent, Intent, String)}</li>
 * <li>Bluetooth Discoverable Status API [DELETE] :
 * {@link ConnectProfile#onDeleteBluetoothDiscoverable(Intent, Intent, String)}</li>
 * <li>NFC Connect API [GET] :
 * {@link ConnectProfile#onGetNFC(Intent, Intent, String)}</li>
 * <li>NFC Connect API [PUT] :
 * {@link ConnectProfile#onPutNFC(Intent, Intent, String)}</li>
 * <li>NFC Connect API [DELETE] :
 * {@link ConnectProfile#onDeleteNFC(Intent, Intent, String)}</li>
 * <li>NFC Connect Status Change Event API [Register] :
 * {@link ConnectProfile#onPutOnNFCChange(Intent, Intent, String, String)}</li>
 * <li>NFC Connect Status Change Event API [Unregister] :
 * {@link ConnectProfile#onDeleteOnNFCChange(Intent, Intent, String, String)}</li>
 * <li>BLE Connect API [GET] :
 * {@link ConnectProfile#onGetBLE(Intent, Intent, String)}</li>
 * <li>BLE Connect API [PUT] :
 * {@link ConnectProfile#onPutBLE(Intent, Intent, String)}</li>
 * <li>BLE Connect API [DELETE] :
 * {@link ConnectProfile#onDeleteBLE(Intent, Intent, String)}</li>
 * <li>BLE Connect Status Change Event API [Register] :
 * {@link ConnectProfile#onPutOnBLEChange(Intent, Intent, String, String)}</li>
 * <li>BLE Connect Status Change Event API [Unregister] :
 * {@link ConnectProfile#onDeleteOnBLEChange(Intent, Intent, String, String)}</li>
 * </ul>
 * 
 * @author NTT DOCOMO, INC.
 */
public abstract class ConnectProfile extends DConnectProfile implements ConnectProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに接続状態フラグを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param enable ON:true、OFF:false
     */
    public static void setEnable(final Intent response, final boolean enable) {
        response.putExtra(PARAM_ENABLE, enable);
    }

    /**
     * メッセージに接続状態を設定する.
     * 
     * @param message メッセージパラメータ
     * @param connectStatus 接続状態パラメータ
     */
    public static void setConnectStatus(final Intent message, final Bundle connectStatus) {
        message.putExtra(PARAM_CONNECT_STATUS, connectStatus);
    }

    /**
     * 接続状態パラメータに接続状態フラグを設定する.
     * 
     * @param connectStatus 接続状態パラメータ
     * @param enable ON: true、OFF: false
     */
    public static void setEnable(final Bundle connectStatus, final boolean enable) {
        connectStatus.putBoolean(PARAM_ENABLE, enable);
    }
}
