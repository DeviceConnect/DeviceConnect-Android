/*
 FailServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestServiceDiscoveryProfileConstants;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Network Service Discovery プロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailServiceDiscoveryProfileTestCase extends RESTfulDConnectTestCase {
    /**
     * POSTメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /servicediscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
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
     * PUTメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /servicediscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * DELETEメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /servicediscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices003() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * deviceidを指定してサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /servicediscovery?deviceid=xxxx
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・servicesに少なくとも1つ以上のサービスが発見されること。
     * ・servicesの中に「Test Success Device」のnameを持ったサービスが存在すること。
     * </pre>
     */
    @Test
    public void testGetServices004() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
            JSONArray services = root.getJSONArray(ServiceDiscoveryProfileConstants.PARAM_SERVICES);
            assertNotNull("services is null.", root);
            assertTrue("services not found.", services.length() > 0);
            boolean isFoundName = false;
            for (int i = 0; i < services.length(); i++) {
                JSONObject service = services.getJSONObject(i);
                String name = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
                String id = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
                String type = service.getString(ServiceDiscoveryProfileConstants.PARAM_TYPE);
                assertNotNull("service.name is null", name);
                assertNotNull("service.id is null", id);
                assertNotNull("service.type is null", type);
                if (name.equals(TestServiceDiscoveryProfileConstants.DEVICE_NAME)) {
                    isFoundName = true;
                }
            }
            if (!isFoundName) {
                fail("Not found Test DevicePlugin.");
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * GETメソッドでサービス追加イベント登録を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /servicediscovery/onservicechange
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testOnServiceChangeInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.setAttribute(ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE);
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
     * POSTメソッドでサービス追加イベント登録を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /servicediscovery/onservicechange
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testOnServiceChangeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.setAttribute(ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE);
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
