/*
 NormalAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Authorizationプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalAuthorizationProfileTestCase extends RESTfulDConnectTestCase {

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    @Override
    protected String getOrigin() {
        return "abc";
    }

    /**
     * クライアント作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・clientIdにstring型の値が返ること。
     * </pre>
     */
    @Test
    public void testCreateClient() throws Exception {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("clientId"), is(notNullValue()));
    }

    /**
     * クライアント作成済みのパッケージについてクライアントを作成し直すテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・異なるclientIdが返ること。
     * </pre>
     */
    @Test
    public void testCreateClientOverwrite() throws Exception {
        String uri = "http://localhost:4035/gotapi/authorization/grant";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response1 = HttpUtil.get(uri, headers);
        assertThat(response1, is(notNullValue()));

        JSONObject json1 = response1.getJSONObject();
        assertThat(json1, is(notNullValue()));
        assertThat(json1.getInt("result"), is(0));
        assertThat(json1.getString("clientId"), is(notNullValue()));


        HttpUtil.Response response2 = HttpUtil.get(uri, headers);
        assertThat(response2, is(notNullValue()));

        JSONObject json2 = response2.getJSONObject();
        assertThat(json2, is(notNullValue()));
        assertThat(json2.getInt("result"), is(0));
        assertThat(json2.getString("clientId"), is(notNullValue()));

        String clientId1 = json1.getString("clientId");
        String clientId2 = json2.getString("clientId");
        assertThat(clientId1, is(not(clientId2)));
    }

    /**
     * アクセストークン取得テストを行う.
     * 1つのスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・クライアント作成に成功すること。
     * ・アクセストークン取得に成功すること。
     * </pre>
     */
    @Test
    public void testRequestAccessToken() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("accessToken"), is(notNullValue()));
    }

    /**
     * アクセストークン取得テストを行う.
     * 複数のスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・クライアント作成に成功すること。
     * ・アクセストークン取得に成功すること。
     * </pre>
     */
    @Test
    public void testRequestAccessTokenMultiScope() throws Exception {
        String clientId = createClientId();
        String appName = "JUnit Test";
        String[] scopes = {
                "battery",
                "serviceDiscovery",
                "serviceInformation"
        };

        String uri = "http://localhost:4035/gotapi/authorization/accessToken";
        uri += "?clientId=" + URLEncoder.encode(clientId, "UTF-8");
        uri += "&scope=" + URLEncoder.encode(combineStr(scopes), "UTF-8");
        uri += "&applicationName=" + URLEncoder.encode(appName, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("accessToken"), is(notNullValue()));
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
        assertThat(json, CoreMatchers.is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
        assertThat(json.getString("clientId"), is(notNullValue()));

        return json.getString("clientId");
    }
}
