/*
 FailServiceInformationProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Service Informationプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class FailServiceInformationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * コンストラクタ.
     * 
     * @param tag テストタグ
     */
    public FailServiceInformationProfileTestCase(final String tag) {
        super(tag);
    }

    /**
     * serviceIdを指定せずにデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceinformation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceUndefinedParameter() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals("1.0", 
                    resp.getString(ServiceInformationProfileConstants.PARAM_VERSION));
            JSONObject connect = resp.getJSONObject(ServiceInformationProfileConstants.PARAM_CONNECT);
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
     * Path: /serviceinformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
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
     * Path: /serviceinformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    public void testGetSystemDeviceInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
