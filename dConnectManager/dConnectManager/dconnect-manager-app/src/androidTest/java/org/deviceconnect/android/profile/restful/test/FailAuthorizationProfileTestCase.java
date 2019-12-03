/*
 FailAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.hamcrest.core.IsNull;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Authorizationプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailAuthorizationProfileTestCase extends RESTfulDConnectTestCase {

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    @Override
    protected String getOrigin() {
        return "fail.restful.junit";
    }

    /**
     * 定義にない属性を指定してクライアント作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetCreateClientUndefinedAttribute() {
        String uri = "http://localhost:4035/gotapi/authorization/grant?def=def";

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにPOSTを指定してクライアント作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetCreateClientInvalidMethodPost() {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        DConnectResponseMessage response = mDConnectSDK.post(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * メソッドにPUTを指定してクライアント作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetCreateClientInvalidMethodPut() {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        DConnectResponseMessage response = mDConnectSDK.put(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * メソッドにDELETEを指定してクライアント作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetCreateClientInvalidMethodDelete() {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        DConnectResponseMessage response = mDConnectSDK.delete(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * clientIdが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoClientId() throws Exception {
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * clientIdに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clintId=&amp;scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyClientId() throws Exception {
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode("", "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * 未登録のclientIdを指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNotRegisteredClientId() throws Exception {
        String clientId = "not_registered_client_id";
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.AUTHORIZATION.getCode()));
    }

    /**
     * scopeが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoScope() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * scopeに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;grantType=authorization_code&amp;scope=&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyScope() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode("", "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * applicationNameが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&amp;scope=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoApplicationName() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * applicationに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;grantType=authorization_code&amp;scope=&amp;applicationName
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyApplicationName() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode("", "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * 定義にない属性を指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;scope=xxxx&amp;applicationName=xxxx&amp;abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenUndefinedAttribute() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");
        uri += "&adb=" + URLEncoder.encode("abc", "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにPOSTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPost() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.post(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
    }

    /**
     * メソッドにPUTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPut() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.put(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
    }

    /**
     * メソッドにDELETEを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /authorization/accessToken?
     *           clientId=xxxx&amp;scope=xxxx&amp;applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodDelete() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        DConnectResponseMessage response = mDConnectSDK.delete(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
    }

    /**
     * clientIdを作成する.
     * @return clientId
     * @throws Exception clientIdの作成に失敗した場合に発生
     */
    private String createClientId() throws Exception {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("clientId"), is(IsNull.notNullValue()));

        return json.getString("clientId");
    }
}
