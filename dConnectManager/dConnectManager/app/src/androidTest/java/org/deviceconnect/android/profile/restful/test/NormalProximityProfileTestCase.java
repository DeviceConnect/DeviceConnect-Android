/*
 NormalProximityProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestProximityProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Proximityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalProximityProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * メソッドにGETを指定してondeviceproximity属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /proximity/ondeviceproximity?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOnDeviceProximityChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ProximityProfileConstants.PROFILE_NAME);
        builder.setAttribute(ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            JSONObject proximity = root.getJSONObject(ProximityProfileConstants.PARAM_PROXIMITY);
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.VALUE),
                    proximity.getDouble(ProximityProfileConstants.PARAM_VALUE));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.MIN), 
                    proximity.getDouble(ProximityProfileConstants.PARAM_MIN));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.MAX), 
                    proximity.getDouble(ProximityProfileConstants.PARAM_MAX));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.THRESHOLD),
                    proximity.getDouble(ProximityProfileConstants.PARAM_THRESHOLD));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 近接センサーによる物の検知のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /proximity/ondeviceproximity?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnDeviceProximity01() {
        try {
            JSONObject event = registerEventCallback(ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);
            JSONObject proximity = event.getJSONObject(ProximityProfileConstants.PARAM_PROXIMITY);
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.VALUE),
                    proximity.getDouble(ProximityProfileConstants.PARAM_VALUE));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.MIN), 
                    proximity.getDouble(ProximityProfileConstants.PARAM_MIN));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.MAX), 
                    proximity.getDouble(ProximityProfileConstants.PARAM_MAX));
            Assert.assertEquals(Double.valueOf(TestProximityProfileConstants.THRESHOLD),
                    proximity.getDouble(ProximityProfileConstants.PARAM_THRESHOLD));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 近接センサーによる物の検知のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /proximity/ondeviceproximity?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnDeviceProximity02() {
        unregisterEventCallback(ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);
    }

    /**
     * メソッドにGETを指定してonuserproximity属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /proximity/onuserproximity?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOnUserProximityChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ProximityProfileConstants.PROFILE_NAME);
        builder.setAttribute(ProximityProfileConstants.ATTRIBUTE_ON_USER_PROXIMITY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            JSONObject proximity = root.getJSONObject(ProximityProfileConstants.PARAM_PROXIMITY);
            Assert.assertEquals(true, proximity.getBoolean(ProximityProfileConstants.PARAM_NEAR));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 近接センサーによる人の検知のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /proximity/onuserproximity?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnUserProximity01() {
        try {
            JSONObject event = registerEventCallback(ProximityProfileConstants.ATTRIBUTE_ON_USER_PROXIMITY);
            JSONObject proximity = event.getJSONObject(ProximityProfileConstants.PARAM_PROXIMITY);
            Assert.assertEquals(true, proximity.getBoolean(ProximityProfileConstants.PARAM_NEAR));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 近接センサーによる人の検知のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /proximity/onuserproximity?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnUserProximity02() {
        unregisterEventCallback(ProximityProfileConstants.ATTRIBUTE_ON_USER_PROXIMITY);
    }

    /**
     * コールバック登録リクエストを送信する.
     * @param attribute コールバックの属性名
     * @return 受信したイベント
     * @throws JSONException JSONの解析に失敗した場合
     */
    private JSONObject registerEventCallback(final String attribute) throws JSONException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ProximityProfileConstants.PROFILE_NAME);
        builder.append("/" + attribute);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        HttpUriRequest request = new HttpPut(builder.toString());
        JSONObject root = sendRequest(request);
        Assert.assertNotNull("root is null.", root);
        Assert.assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));
        JSONObject event = waitForEvent();
        Assert.assertNotNull("event is null.", event);
        return event;
    }

    /**
     * コールバック解除リクエストを送信する.
     * @param attribute コールバックの属性名
     */
    private void unregisterEventCallback(final String attribute) {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ProximityProfileConstants.PROFILE_NAME);
        builder.append("/" + attribute);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
