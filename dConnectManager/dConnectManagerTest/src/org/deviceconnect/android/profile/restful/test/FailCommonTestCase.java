/*
 FailCommonTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * プロファイル共通の異常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class FailCommonTestCase extends RESTfulDConnectTestCase {

//    /**
//     * テスト用デバイス名: {@value}.
//     */
//    private static final String DEVICE_NAME_SPECIAL_CHARACTERS = "Test Service ID Special Characters";

    /**
     * コンストラクタ.
     * 
     * @param tag テストタグ
     */
    public FailCommonTestCase(final String tag) {
        super(tag);
    }

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
    public void testEmptyServiceIdWithoutEqual() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
     * @throws UnsupportedEncodingException サービスIDのURLエンコーディングに失敗した場合
     */
    public void testInvalidServiceIdNoExist() throws UnsupportedEncodingException {
        final String serviceId = URLEncoder.encode("!#$'()-~¥@[;+:*],._", "UTF-8");
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + serviceId);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testInvalidAccessTokenNotEncoded() {
        final String accessToken = "!#$'()-~¥@[;+:*],._";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + accessToken);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_CLIENT_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testRequestWithIllegalAccessToken() {
        final String illegalAccessToken = "illegal_access_token";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + BatteryProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + illegalAccessToken);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_CLIENT_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testNotSupportedProfileIncludedScope() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/abc");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_PROFILE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * デバイスプラグインのサポートしていないプロファイルに対してアクセスするテストを行う.
     * OAuthのスコープにも含まれていない.
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
    public void testNotSupportedProfileNotIncludedScope() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/def");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.SCOPE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testInvalidAccessEmptyProfileEmptyInterfaceEmptyAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("///?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        HttpUriRequest request = new HttpGet(builder.toString());
        HttpResponse response = requestHttpResponse(request);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
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
    public void testInvalidAccessEmptyProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("//battery/charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testInvalidAccessEmptyInterface() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/battery//charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
    public void testInvalidAccessEmptyAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/battery//charging?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
