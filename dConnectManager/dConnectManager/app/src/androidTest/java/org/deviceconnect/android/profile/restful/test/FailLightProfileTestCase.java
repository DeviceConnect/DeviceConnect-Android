/*
 FailLightProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
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
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.test.plugin.profile.TestLightProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

/**
 * Lightプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailLightProfileTestCase extends RESTfulDConnectTestCase implements TestLightProfileConstants {

    /**
     * serviceIdを指定せずにライト情報要求を送信するテスト.
     * 【HTTP通信】
     * Method: GET
     * Path: /light
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライト情報要求を送信するテスト.
     * 【HTTP通信】
     * Method: GET
     * Path: /light?serviceId=
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してライト情報要求を送信するテスト.
     * 【HTTP通信】
     * Method: GET
     * Path: /light?serviceId=123456789
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライト情報要求を送信するテスト.
     * 【HTTP通信】
     * Method: GET
     * Path: /light?serviceId=123456789&serviceId=xxxxxx
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=123456789&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=123456789&serviceId=xxxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを指定せずにライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightNoLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに空文字を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないidを指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=123456789&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないidを指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=123456789&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに空文字を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorを2重に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=GGGGGG&color=xxxx&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに全角の文字列を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=あいうえお&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidColor1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorにFFFFFFFを指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=FFFFFFF&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidColor2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに16進数以外の文字列を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=ZZZZZZ&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidColor3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに空文字を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessを2重に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=aa&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに-1を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=-1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidBrightness1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに1.1を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=1.1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidBrightness2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに全角の文字列を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=あいうえお&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidBrightness3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに空文字を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingを２重に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=&flashing=1000,1002,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに-1を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=-1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidFlashing1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに全角の文字列を指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=あいうえお
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidFlashing2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を空文字に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=100,,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidFlashing3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を-100に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=100,-100,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidFlashing4() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの最後を,に指定してライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxx&lightId=xxx&color=xxxx&brightness=0.5&flashing=100,100,100,
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidFlashing5() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しないIDを指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=123456789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しないIDを指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=123456789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを指定せずにライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightNoLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに空文字を指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightEmptyLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないIDを指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=1234556789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightInvalidLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを2重に指定してライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=1234556789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightDuplicatedLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }




    /**
     * serviceIdを指定せずにライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?lightId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=&name=xxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=123456789&lightId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=123456789&serviceId=xxxx&name=xxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを指定せずにライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightNoLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないidを指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=123456789&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないidを指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=123456789&lightId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameを指定せずにライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightNoName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&color=ff0000&name=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameを指定せずにライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=&name=xxxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorを2重に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=GGGGGG&color=xxxx&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに全角の文字列を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=あいうえお&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidColor1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorにFFFFFFFを指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=FFFFFFF&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidColor2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに16進数以外の文字列を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=ZZZZZZ&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidColor3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessを2重に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=aa&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに-1を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=-1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidBrightness1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに1.1を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=1.1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidBrightness2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに全角の文字列を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=あいうえお&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidBrightness3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに空文字を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingを２重に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=&flashing=1000,1002,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに-1を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=-1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに全角の文字列を指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=あいうえお
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を空文字に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を-100に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,-100,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing4() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの最後を,に指定してライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxx&lightId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,100,100,
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing5() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにライトグループ情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light/group
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightGroupNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light/group?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightGroupEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * serviceIdに存在しないIDを指定してライトグループ情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light/group?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightGroupInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライトグループ情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light/group?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLightGroupDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * serviceIdを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=123456789&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=123456789&serviceId=xxxx&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupNoLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupEmptyGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに存在しないidを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=123456789&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに存在しないidを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=123456789&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupDuplicatedGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupEmptyColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=GGGGGG&color=xxxx&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupDuplicatedColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=あいうえお&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidColor1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorにFFFFFFFを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=FFFFFFF&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidColor2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに16進数以外の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=ZZZZZZ&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidColor3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupEmptyBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=aa&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupDuplicatedBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに-1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=-1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidBrightness1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに1.1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=1.1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidBrightness2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=あいうえお&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidBrightness3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupEmptyFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingを２重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=&flashing=1000,1002,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupDuplicatedFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに-1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=-1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidFlashing1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=あいうえお
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidFlashing2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を空文字に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=100,,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidFlashing3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を-100に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=100,-100,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidFlashing4() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの最後を,に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=xxxx&brightness=0.5&flashing=100,100,100,
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupInvalidFlashing5() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * serviceIdを指定せずにライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しないIDを指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=123456789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しないIDを指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=123456789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを指定せずにライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupNoLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに空文字を指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupEmptyLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "");
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdに存在しないIDを指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=1234556789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupInvalidLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdを2重に指定してライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=1234556789&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupDuplicatedLightId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * serviceIdを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?groupId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=&name=xxx&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=123456789&groupId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=123456789&serviceId=xxxx&name=xxx&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupNoGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに存在しないidを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=123456789&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに存在しないidを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=123456789&lightId=xxx&name=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupNoName() {
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
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&color=ff0000&name=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * nameを指定せずにライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=&name=xxxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=GGGGGG&color=xxxx&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedColor() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=あいうえお&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidColor1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorにFFFFFFFを指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=FFFFFFF&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidColor2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * colorに16進数以外の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=ZZZZZZ&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidColor3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessを2重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=aa&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedBrightness() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに-1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=-1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidBrightness1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに1.1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=1.1&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidBrightness2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * brightnessに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=あいうえお&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidBrightness3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに空文字を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupEmptyFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingを２重に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=&flashing=1000,1002,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupDuplicatedFlashing() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに-1を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=-1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidFlashing1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingに全角の文字列を指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=あいうえお
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidFlashing2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を空文字に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidFlashing3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの一部を-100に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,-100,100
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidFlashing4() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * flashingの最後を,に指定してライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light/group?serviceId=xxx&groupId=xxx&name=xxx&color=xxxx&brightness=0.5&flashing=100,100,100,
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroupInvalidFlashing5() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setAttribute(LightProfile.ATTRIBUTE_GROUP);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }



    /**
     * serviceIdを指定せずにライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?groupName=xxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=&groupName=xxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しない値を指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdsを指定せずにライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateNoLightIds() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdsに空文字を指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateEmptyLightIds() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, "");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdsを2重に指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateDuplicatedLightIds() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * lightIdsに存在しないgroupIdを指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=abc,def,ghi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateInvalidLightIds1() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, "abc,def,ghi");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdsに不正なフォーマットを指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=test_light_id1,,test_light_id3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateInvalidLightIds2() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, "test_light_id1,,test_light_id3");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * lightIdsに不正なフォーマットを指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=123456789&groupName=xxx&lightIds=test_light_id1,test_light_id2,
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateInvalidLightIds3() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, "test_light_id1,test_light_id2,");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * groupNameを指定せずにライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=xxxx&lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateNoGroupName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupNameに空文字を指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=xxxx&lightIds=xxx,xxx,xxx&groupName=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateEmptyGroupName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, "");
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupNameを2重に指定してライトグループ作成要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light/group/create?serviceId=xxxx&lightIds=xxx,xxx,xxx&groupName=&groupName=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreateDuplicatedGroupName() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CREATE);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, "");
        builder.addParameter(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * serviceIdを指定せずにライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに空文字を指定してライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdに存在しないIDを指定してライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=123456789&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=123456789&serviceId=xxx&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdを指定せずにライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearNoGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに空文字を指定してライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=xxx&groupId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearEmptyGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "");
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * groupIdに空文字を指定してライトグループ削除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=xxx&groupId=&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClearDuplicatedGroupId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.setInterface(LightProfile.INTERFACE_GROUP);
        builder.setAttribute(LightProfile.ATTRIBUTE_CLEAR);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_GROUP_ID, "");
        builder.addParameter(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultError(response);
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
