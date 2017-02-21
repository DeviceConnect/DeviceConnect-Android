/*
 FailAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.hamcrest.core.IsNull;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    /**
     * アプリケーション名: {@value}.
     */
    private static final String TEST_APPLICATION_NAME = "dConnectManagerTest";

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    @Override
    protected String getOrigin() {
        return "abc";
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        builder.addParameter("def", "def");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
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
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * clientIdが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoClientId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * clientIdに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clintId=&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyClientId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * 未登録のclientIdを指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNotRegisteredClientId() throws Exception {
        final String clientId = "not_registered_client_id";

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.AUTHORIZATION.getCode()));
    }

    /**
     * scopeが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoScope() throws Exception {
        String clientId = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
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
     *           clientId=xxxx&grantType=authorization_code&scope=&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyScope() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * applicationNameが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&scope=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoApplicationName() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
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
     *           clientId=xxxx&grantType=authorization_code&scope=&applicationName
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyApplicationName() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, "");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
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
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenUndefinedAttribute() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);
        builder.addParameter("abc", "abc");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにPOSTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /authorization/accessToken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPost() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
    }

    /**
     * メソッドにPUTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /authorization/accessToken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPut() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
    }

    /**
     * メソッドにDELETEを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /authorization/accessToken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodDelete() throws Exception {
        String client = createClientId();

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
    }

    /**
     * clientIdを作成する.
     * @return clientId
     * @throws Exception clientIdの作成に失敗した場合に発生
     */
    private String createClientId() throws Exception {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", "abc");

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("clientId"), is(IsNull.notNullValue()));

        return json.getString("clientId");
    }
}
