/*
 FailAuthorizationProfileTestCase.java
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
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    protected boolean isSearchDevices() {
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
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        builder.addParameter("def", "def");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * clientIdが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoClientId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * clientIdに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?clintId=&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyClientId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 未登録のclientIdを指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNotRegisteredClientId() {
        final String clientId = "not_registered_client_id";

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.AUTHORIZATION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * scopeが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?clientId=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoScope() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * scopeに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&grantType=authorization_code&scope=&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyScope() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * applicationNameが無い状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?clientId=xxxx&scope=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenNoApplicationName() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * applicationに空文字を指定した状態でアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&grantType=authorization_code&scope=&applicationName
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenEmptyApplicationName() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, "");

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenUndefinedAttribute() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);
        builder.addParameter("abc", "abc");

        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPost() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodPut() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してアクセストークン作成を行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetRequestAccessTokenInvalidMethodDelete() {
        String client = createClient();

        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, client);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, "battery");
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME,
                TEST_APPLICATION_NAME);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
