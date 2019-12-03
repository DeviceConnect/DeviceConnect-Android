/*
 NormalAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;

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
        return "normal.restful.junit";
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
        String uri = "http://localhost:4035/gotapi/authorization/grant?def=def";

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString("clientId"), is(notNullValue()));
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
        String clientId1 = createClientId();
        String clientId2 = createClientId();
        assertThat(clientId1, is(not(clientId2)));
    }

    /**
     * アクセストークン取得テストを行う.
     * 1つのスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&amp;amp;scope=xxxx&amp;applicationName=xxxx
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

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString("accessToken"), is(notNullValue()));
    }

    /**
     * アクセストークン取得テストを行う.
     * 複数のスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accessToken?clientId=xxxx&amp;amp;scope=xxxx&amp;applicationName=xxxx
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

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString("accessToken"), is(notNullValue()));
    }

    /**
     * clientIdを作成する.
     * @return clientId
     * @throws Exception clientIdの作成に失敗した場合に発生
     */
    private String createClientId() throws Exception {
        String uri = "http://localhost:4035/gotapi/authorization/grant?def=def";

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString("clientId"), is(notNullValue()));

        return response.getString("clientId");
    }
}
