/*
 FailConnectionProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.ConnectionProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ConnectionProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Connectionプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailConnectionProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdが無い状態でWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・enableがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getBoolean(ConnectionProfile.PARAM_ENABLE), is(false));
    }

    /**
     * serviceIdが無い状態でWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定してWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/wifi?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectWifiInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonWifiChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onWifiChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonWifiChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onWifiChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonWifiChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onWifiChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonWifiChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onWifiChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonWifiChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onWifiChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonWifiChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onWifiChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonWifiChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onWifiChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonWifiChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onWifiChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定して/connection/onWifiChangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/onWifiChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnWifiChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・enableがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getBoolean(ConnectionProfile.PARAM_ENABLE), is(false));
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/bluetooth?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectBluetoothInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth/discoverable
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth/discoverable?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth/discoverable?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/bluetooth/discoverable?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth/discoverable
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth/discoverable?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth/discoverable?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/bluetooth/discoverable?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectionProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonBluetoothChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBluetoothChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonBluetoothChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBluetoothChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonBluetoothChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBluetoothChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonBluetoothChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBluetoothChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonBluetoothChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBluetoothChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonBluetoothChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBluetoothChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonBluetoothChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBluetoothChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonBluetoothChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBluetoothChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定して/connection/onBluetoothChangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/onBluetoothChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBluetoothChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・enableがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getBoolean(ConnectionProfile.PARAM_ENABLE), is(false));
    }

    /**
     * serviceIdが無い状態でNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定してNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/nfc?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectNFCInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonNfcChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onNfcChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonNfcChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onNfcChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonNfcChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onNfcChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonNfcChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onNfcChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonNfcChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onNfcChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonNfcChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onNfcChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonNfcChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onNfcChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonNfcChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onNfcChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定して/connection/onNfcChangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/onNfcChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnNFCChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connection/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・enableがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getBoolean(ConnectionProfile.PARAM_ENABLE), is(false));
    }

    /**
     * serviceIdが無い状態でBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定してBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/ble?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectBLEInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonBleChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBleChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonBleChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBleChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonBleChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBleChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonBleChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connection/onBleChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonBleChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBleChange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonBleChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBleChange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonBleChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBleChange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonBleChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connection/onBleChange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドをPOSTに指定して/connection/onBleChangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connection/onBleChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBLEChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ConnectionProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectionProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
