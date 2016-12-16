/*
 FailLightProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.test.plugin.profile.TestLightProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyLightId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        // TODO lightIdは省略可になったはず・・・・

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightInvalidLightId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightEmptyFlashing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightDuplicatedFlashing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        // TODO lightIdは省略可になったはず・・・・

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        // TODO

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, "123456789");
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightEmptyName() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedName() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, "");
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "GGGGGG");
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "あいうえお");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "FFFFFFF");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, "ZZZZZZ");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "aaa");
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "-1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "1.1");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, "あいうえお");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightDuplicatedFlashing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "");
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "-1");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "あいうえお");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,,100");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,-100,100");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightInvalidFlashing5() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, "100,100,100,");

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
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
}
