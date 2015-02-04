/*
 FailSystemProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestSystemProfileConstants;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Systemプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class FailSystemProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * コンストラクタ.
     * 
     * @param tag テストタグ
     */
    public FailSystemProfileTestCase(final String tag) {
        super(tag);
    }

    /**
     * 未定義のパラメータを指定してシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system?abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・versionにString型の値が返ってくること。
     * </pre>
     */
    public void testGetSystemUndefinedParameter() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.addParameter("abc", "abc");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSystemProfileConstants.VERSION, 
                    resp.getString(SystemProfileConstants.PARAM_VERSION));
            JSONArray support = resp.getJSONArray(SystemProfileConstants.PARAM_SUPPORTS);
            assertNotNull("support is null.", support);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * POSTメソッドでシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * PUTメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * DELETEメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 未定義のパラメータを指定してデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device?serviceId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceUndefinedParameter() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals("1.0", 
                    resp.getString(SystemProfileConstants.PARAM_VERSION));
            JSONObject connect = resp.getJSONObject(SystemProfileConstants.PARAM_CONNECT);
            assertNotNull(connect);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * POSTメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * PUTメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * DELETEメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * GETメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/events?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceEventsInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * POSTメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/events?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceEventsInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * PUTメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /system/device/events?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceEventsInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * GETメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceKeywordInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * POSTメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceKeywordInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * DELETEメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceKeywordInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * GETメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceWakeupInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * POSTメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceWakeupInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * DELETEメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceWakeupInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
