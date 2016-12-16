/*
 NormalAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    protected boolean isSearchServices() {
        return false;
    }

    @Override
    protected String getOrigin() {
        return "abc";
    }


    private String combineStr(final String[] scopes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(scopes[i].trim());
        }
        return builder.toString();
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
    public void testCreateClient() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfile.ATTRIBUTE_GRANT);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AuthorizationProfile.PARAM_CLIENT_ID), is(notNullValue()));
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
    public void testCreateClientOverwrite() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfile.ATTRIBUTE_GRANT);

        DConnectResponseMessage response1 = mDConnectSDK.get(builder.build());
        assertThat(response1, is(notNullValue()));
        assertThat(response1.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response1.getString(AuthorizationProfile.PARAM_CLIENT_ID), is(notNullValue()));

        DConnectResponseMessage response2 = mDConnectSDK.get(builder.build());
        assertThat(response2, is(notNullValue()));
        assertThat(response2.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response2.getString(AuthorizationProfile.PARAM_CLIENT_ID), is(notNullValue()));

        String clientId1 = response1.getString(AuthorizationProfile.PARAM_CLIENT_ID);
        String clientId2 = response2.getString(AuthorizationProfile.PARAM_CLIENT_ID);
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
    public void testRequestAccessToken() {
        DConnectSDK.URIBuilder builder1 = mDConnectSDK.createURIBuilder();
        builder1.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder1.setAttribute(AuthorizationProfile.ATTRIBUTE_GRANT);

        DConnectResponseMessage response1 = mDConnectSDK.get(builder1.build());
        assertThat(response1, is(notNullValue()));
        assertThat(response1.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response1.getString(AuthorizationProfile.PARAM_CLIENT_ID), is(notNullValue()));

        String clientId = response1.getString(AuthorizationProfile.PARAM_CLIENT_ID);
        String appName = "JUnit Test";
        String[] scopes = {
                BatteryProfile.PROFILE_NAME
        };

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfile.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, appName);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, combineStr(scopes));

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AuthorizationProfile.PARAM_ACCESS_TOKEN), is(notNullValue()));
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
    public void testRequestAccessTokenMultiScope() {
        DConnectSDK.URIBuilder builder1 = mDConnectSDK.createURIBuilder();
        builder1.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder1.setAttribute(AuthorizationProfile.ATTRIBUTE_GRANT);

        DConnectResponseMessage response1 = mDConnectSDK.get(builder1.build());
        assertThat(response1, is(notNullValue()));
        assertThat(response1.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response1.getString(AuthorizationProfile.PARAM_CLIENT_ID), is(notNullValue()));

        String clientId = response1.getString(AuthorizationProfile.PARAM_CLIENT_ID);
        String appName = "JUnit Test";
        String[] scopes = {
                BatteryProfile.PROFILE_NAME,
                ServiceDiscoveryProfile.PROFILE_NAME,
                ServiceInformationProfile.PROFILE_NAME
        };

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfile.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfile.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, appName);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, combineStr(scopes));

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AuthorizationProfile.PARAM_ACCESS_TOKEN), is(notNullValue()));
    }
}
