/*
 FailDeviceOrientationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * DeviceOrientationプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class FailDeviceOrientationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * コンストラクタ.
     * 
     * @param tag テストタグ
     */
    public FailDeviceOrientationProfileTestCase(final String tag) {
        super(tag);
    }

    /**
     * serviceIdが無い状態でondeviceorientation属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testPutOnDeviceOrientation001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * serviceIdが空状態でondeviceorientation属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testPutOnDeviceOrientation002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * 存在しないserviceIdでondeviceorientation属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testPutOnDeviceOrientation003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * 定義にない属性を指定してondeviceorientation属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    public void testPutOnDeviceOrientation004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * serviceIdを2重に指定してondeviceorientation属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testPutOnDeviceOrientation005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * serviceIdが無い状態でondeviceorientation属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /deviceorientation/ondeviceorientation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testDeleteOnDeviceOrientation001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * serviceIdが空状態でondeviceorientation属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /deviceorientation/ondeviceorientation?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testDeleteOnDeviceOrientation002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * 存在しないserviceIdでondeviceorientation属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /deviceorientation/ondeviceorientation?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testDeleteOnDeviceOrientation003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * 定義にない属性を指定してondeviceorientation属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /deviceorientation/ondeviceorientation?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    public void testDeleteOnDeviceOrientation004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * serviceIdを2重に指定してondeviceorientation属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /deviceorientation/ondeviceorientation?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testDeleteOnDeviceOrientation005() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
     * メソッドをPOSTに指定して/deviceorientation/ondeviceorientationにアクセスするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /deviceorientation/ondeviceorientation?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testOnDeviceOrientationInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
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
