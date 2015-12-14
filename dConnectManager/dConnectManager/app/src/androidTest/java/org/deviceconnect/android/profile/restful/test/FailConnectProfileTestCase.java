/*
 FailConnectProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Connectプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailConnectProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdが無い状態でWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・powerがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
            assertEquals(true, root.getBoolean(ConnectProfileConstants.PARAM_ENABLE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してWiFi機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/wifi?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetWifi005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してWiFi機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/wifi?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWifi005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/wifi?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteWifi005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定してWiFi機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/wifi?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectWifiInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_WIFI);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonwifichange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onwifichange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonwifichange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onwifichange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonwifichange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onwifichange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonwifichange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onwifichange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonwifichange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onwifichange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWifiChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonwifichange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onwifichange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonwifichange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onwifichange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonwifichange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onwifichange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonwifichange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onwifichange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonwifichange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onwifichange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWifiChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをGETに指定して/connect/onwifichangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/onwifichange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnWifiChangeInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定して/connect/onwifichangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/onwifichange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnWifiChangeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_WIFI_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・powerがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
            assertEquals(true, root.getBoolean(ConnectProfileConstants.PARAM_ENABLE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBluetooth機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/bluetooth?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBluetooth005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetooth005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetooth005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/bluetooth?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectBluetoothInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLUETOOTH);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBluetooth機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/bluetooth/discoverable?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBluetoothAvailable005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBluetooth機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/bluetooth/discoverable?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBluetoothAvailable005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setInterface(ConnectProfileConstants.INTERFACE_BLUETOOTH);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_DISCOVERABLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonbluetoothchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onbluetoothchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonbluetoothchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onbluetoothchange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonbluetoothchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onbluetoothchange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonbluetoothchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onbluetoothchange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonbluetoothchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onbluetoothchange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBluetoothChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonbluetoothchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onbluetoothchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonbluetoothchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onbluetoothchange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonbluetoothchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onbluetoothchange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonbluetoothchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onbluetoothchange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonbluetoothchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onbluetoothchange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBluetoothChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをGETに指定して/connect/onbluetoothchangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/onbluetoothchange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBluetoothChangeInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定して/connect/onbluetoothchangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/onbluetoothchange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBluetoothChangeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・powerがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
            assertEquals(true, root.getBoolean(ConnectProfileConstants.PARAM_ENABLE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してNFC機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/nfc?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetNFC005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してNFC機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/nfc?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutNFC005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/nfc?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNFC005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定してNFC機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/nfc?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectNFCInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_NFC);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonnfcchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onnfcchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonnfcchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onnfcchange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonnfcchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onnfcchange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonnfcchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onnfcchange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonnfcchange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onnfcchange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnNFCChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonnfcchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onnfcchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonnfcchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onnfcchange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonnfcchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onnfcchange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonnfcchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onnfcchange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonnfcchange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onnfcchange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnNFCChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをGETに指定して/connect/onnfcchangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/onnfcchange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnNFCChangeInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定して/connect/onnfcchangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/onnfcchange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnNFCChangeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_NFC_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・powerがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
            assertEquals(true, root.getBoolean(ConnectProfileConstants.PARAM_ENABLE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBLE機能有効状態(ON/OFF)取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/ble?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBLE005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBLE機能有効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/ble?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBLE005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/ble?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBLE005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定してBLE機能無効化テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/ble?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectBLEInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_BLE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonblechange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onblechange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonblechange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onblechange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonblechange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onblechange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonblechange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onblechange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonblechange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /connect/onblechange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnBLEChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonblechange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onblechange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonblechange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onblechange?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonblechange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onblechange?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonblechange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onblechange?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonblechange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /connect/onblechange?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnBLEChange005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをGETに指定して/connect/onblechangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /connect/onblechange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBLEChangeInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドをPOSTに指定して/connect/onblechangeにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /connect/onblechange?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testConnectOnBLEChangeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ConnectProfileConstants.PROFILE_NAME);
        builder.setAttribute(ConnectProfileConstants.ATTRIBUTE_ON_BLE_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
