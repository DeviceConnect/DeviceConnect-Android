/*
 NormalUniqueProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * 独自プロファイルの正常系テスト.
 * <p>
 * 各メソッドについてリクエストが通知されるかどうかのみをテストする.
 * </p>
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalUniqueProfileTestCase extends RESTfulDConnectTestCase {

    /** プロファイル名: {@value} . */
    private static final String PROFILE_NAME = "unique";

    /** インターフェース名: {@value} . */
    private static final String INTERFACE_TEST = "test";

    /** 属性名: {@value} . */
    private static final String ATTIBUTE_PING = "ping";

    /** パラメータ: {@value} . */
    private static final String PARAM_PATH = "path";

    /** メソッド名: {@value} . */
    private static final String METHOD_GET = "GET";

    /** メソッド名: {@value} . */
    private static final String METHOD_POST = "POST";

    /** メソッド名: {@value} . */
    private static final String METHOD_PUT = "PUT";

    /** メソッド名: {@value} . */
    private static final String METHOD_DELETE = "DELETE";

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /unique
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetUnique() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_GET, null, null)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /unique/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetUniquePing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_GET, null, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /unique/test/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetUniqueTestPing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setInterface(INTERFACE_TEST);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_GET, INTERFACE_TEST, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /unique
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostUnique() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_POST, null, null)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /unique/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostUniquePing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_POST, null, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /unique/test/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostUniqueTestPing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setInterface(INTERFACE_TEST);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_POST, INTERFACE_TEST, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /unique
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutUnique() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_PUT, null, null)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /unique/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutUniquePing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_PUT, null, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /unique/test/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutUniqueTestPing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setInterface(INTERFACE_TEST);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_PUT, INTERFACE_TEST, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /unique
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteUnique() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_DELETE, null, null)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /unique/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteUniquePing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_DELETE, null, ATTIBUTE_PING)));
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /unique/test/ping
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteUniqueTestPing() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PROFILE_NAME);
        builder.setInterface(INTERFACE_TEST);
        builder.setAttribute(ATTIBUTE_PING);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_PATH), is(createPath(METHOD_DELETE, INTERFACE_TEST, ATTIBUTE_PING)));
    }

    /**
     * 指定したAPIのパスを生成する.
     * 
     * @param method メソッド名
     * @param inter インターフェース名
     * @param attribute 属性名
     * @return パス
     */
    private static String createPath(final String method, final String inter, final String attribute) {
        StringBuilder builder = new StringBuilder();
        builder.append(method);
        builder.append(" /");
        builder.append(PROFILE_NAME);
        if (inter != null) {
            builder.append("/");
            builder.append(inter);
        }
        if (attribute != null) {
            builder.append("/");
            builder.append(attribute);
        }
        return builder.toString();
    }
}
