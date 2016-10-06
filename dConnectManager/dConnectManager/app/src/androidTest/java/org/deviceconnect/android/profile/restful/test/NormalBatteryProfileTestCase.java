/*
 NormalBatteryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Batteryプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalBatteryProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * バッテリー全属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingがfalseで返ってくること。
     * ・chargingtimeが50000で返ってくること。
     * ・dischargingtimeが10000で返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBattery() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * バッテリーlevel属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/level?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryLevel() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_LEVEL);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * バッテリーcharging属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/charging?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryCharging() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_CHARGING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * バッテリーchargingtime属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/chargingtime?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingtimeが50000で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryChargingTime() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_CHARGING_TIME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * バッテリーdischargingtime属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/dischargingtime?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingtimeが50000で返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryDishargingTime() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_DISCHARGING_TIME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * バッテリーonchargingchangeを登録するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /battery/onchargingchange?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBatteryOnChargingChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
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
     * バッテリーonchargingchangeを解除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /battery/onchargingchange?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBatteryOnChargingChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

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
     * onbatterychange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /battery/onbatterychange?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBatteryOnBatteryChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

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
     * onbatterychange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /battery/onbatterychange?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBatteryOnBatteryChange() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
