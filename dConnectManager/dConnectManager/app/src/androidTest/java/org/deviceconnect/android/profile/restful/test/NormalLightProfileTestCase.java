/*
 NormalLightProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.test.plugin.profile.TestLightProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Lightプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalLightProfileTestCase extends RESTfulDConnectTestCase implements 
        TestLightProfileConstants {

    /**
     * ライト情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・lightsにライト情報が格納されていること。
     * ・lightIdにライトID(test_light_id)が格納されていること。
     * ・nameにライト名(test_light_name)が格納されていること。
     * ・onにライトの状態が格納されていること。
     * </pre>
     */
    @Test
    public void testGetLight() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
            assertTrue(response.has(LightProfile.PARAM_LIGHTS));
            JSONArray lights = response.getJSONArray(LightProfile.PARAM_LIGHTS);
            assertEquals("lights length is not equals.", lights.length(), 1);
            JSONObject light = lights.getJSONObject(0);
            assertEquals("lightId is not equals.", LIGHT_ID, light.getString(LightProfile.PARAM_LIGHT_ID));
            assertEquals("name is not equals.", LIGHT_NAME, light.getString(LightProfile.PARAM_NAME));
            assertEquals("on is not equals.", LIGHT_ON, light.getBoolean(LightProfile.PARAM_ON));
            assertEquals("config is not equals.", LIGHT_CONFIG, light.getString(LightProfile.PARAM_CONFIG));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLight() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLight() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxxx&lightId=xxx&name=xxxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light/group?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・lightGroupsにライトグループ情報が格納されていること。
     * ・lightsにライト情報が格納されていること。
     * ・lightIdにライトID(test_light_id)が格納されていること。
     * ・nameにライト名(test_light_name)が格納されていること。
     * ・onにライトの状態が格納されていること。
     * </pre>
     */
    @Test
    public void testGetLightGroup() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
            assertTrue(response.has(LightProfile.PARAM_LIGHT_GROUPS));
            JSONArray lightGroups = response.getJSONArray(LightProfile.PARAM_LIGHT_GROUPS);
            assertEquals("lightGroups length is not equals.", lightGroups.length(), 1);
            JSONObject lightGroup = lightGroups.getJSONObject(0);
            assertEquals("groupId is not equals.", LIGHT_GROUP_ID, lightGroup.getString(LightProfile.PARAM_GROUP_ID));
            assertEquals("groupName is not equals.", LIGHT_GROUP_NAME, lightGroup.getString(LightProfile.PARAM_NAME));
            JSONArray lights = lightGroup.getJSONArray(LightProfile.PARAM_LIGHTS);
            JSONObject light = lights.getJSONObject(0);
            assertEquals("lightId is not equals.", LIGHT_ID, light.getString(LightProfile.PARAM_LIGHT_ID));
            assertEquals("name is not equals.", LIGHT_NAME, light.getString(LightProfile.PARAM_NAME));
            assertEquals("on is not equals.", LIGHT_ON, light.getBoolean(LightProfile.PARAM_ON));
            assertEquals("config is not equals.", LIGHT_CONFIG, light.getString(LightProfile.PARAM_CONFIG));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxxx&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroup() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group?serviceId=xxxx&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroup() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxxx&groupId=xxx&name=xxxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroup() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=xxxx&groupName=xxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・groupIdが格納されていること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreate() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
            assertEquals("groupId is not equals.", LIGHT_NEW_GROUP_ID, response.getString(LightProfile.PARAM_GROUP_ID));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=xxxx&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClear() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 数値から文字列の色情報を作成する.
     * @param color 色情報
     * @return 文字列(RRGGBB)
     */
    private String convertColor(final Integer color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format("%02x%02x%02x", r, g, b);
    }

    /**
     * 点滅情報を配列から文字列を作成する.
     * @param flashing 点滅情報
     * @return 点滅情報の文字列
     */
    private String convertFlashing(final long[] flashing) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < flashing.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(flashing[i]);
        }
        return sb.toString();
    }

    /**
     * ライトIDリストから文字列を作成する.
     * @param lightIds ライトIDリスト
     * @return ライトIDリストの文字列
     */
    private String convertLightIds(final String[] lightIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lightIds.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(lightIds[i]);
        }
        return sb.toString();
    }
}
