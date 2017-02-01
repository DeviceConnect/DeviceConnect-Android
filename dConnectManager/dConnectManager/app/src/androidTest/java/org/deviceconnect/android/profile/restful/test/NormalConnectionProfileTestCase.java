/*
 NormalConnectionProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ConnectionProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Connectionプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalConnectionProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * WiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * WiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * WiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * WiFi機能有効状態変化イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onWifiChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifiChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * WiFi機能有効状態変化イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onWifiChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifiChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth機能有効状態変化イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onBluetoothChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth機能有効状態変化イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onBluetoothChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth検索可能状態を有効にするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothDiscoverable() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Bluetooth検索可能状態を無効にするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothDiscoverable() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * NFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * NFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * NFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * NFC機能有効状態変化イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onNfcChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFCChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * NFC機能有効状態変化イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onNfcChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFCChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * BLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * BLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * BLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * BLE機能有効状態変化イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onBleChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLEChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * BLE機能有効状態変化イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onBleChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLEChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ConnectionProfileConstants.PROFILE_NAME);
        builder.append("/" + ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
