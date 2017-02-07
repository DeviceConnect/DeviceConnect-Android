/*
 FailCommonTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * プロファイル共通の異常系テスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailCommonTestCase extends RESTfulDConnectTestCase {

    /**
     * イコール記号なしのパラメータを指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testEmptyServiceIdWithoutEqual() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * サービスIDとして特殊文字を含む文字列を指定するテスト.
     * <p>
     * ただし、/ = ? & % ^ | ` " { } < >を除く.
     * </p>
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId=&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidServiceIdNoExist() {
        final String serviceId = "no_exist";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + serviceId);

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * アクセストークンとして特殊文字を含む文字列を指定するテスト.
     * <p>
     * ただし、/ = ? & % ^ | ` " { } < >を除く.
     * </p>
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId=&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidAccessTokenNotEncoded() {
        final String accessToken = "!#$'()-~¥@[;+:*],._";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + accessToken);

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_CLIENT_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 不正なアクセストークンでリクエストを送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testRequestWithIllegalAccessToken() {
        final String illegalAccessToken = "illegal_access_token";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + illegalAccessToken);

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_CLIENT_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * デバイスプラグインのサポートしていないプロファイルに対してアクセスするテストを行う.
     * ただし、OAuthのスコープには含まれている.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testNotSupportedProfileIncludedScope() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/abc");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * デバイスプラグインのサポートしていないプロファイルに対してアクセスするテストを行う.
     * OAuthのスコープにも含まれていない.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /def
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testNotSupportedProfileNotIncludedScope() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/def");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.SCOPE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * リクエストURIのprofile、interfaceおよびattributeに空文字を指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: //
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidAccessEmptyProfileEmptyInterfaceEmptyAttribute() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("///?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * リクエストURIのprofileに空文字を指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: //
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidAccessEmptyProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("//battery/charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        // TODO
        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * リクエストURIのinterfaceに空文字を指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: //
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidAccessEmptyInterface() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/battery//charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        // TODO
        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * リクエストURIのattributeに空文字を指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: //
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testInvalidAccessEmptyAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/battery//charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        // TODO
        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }
}
