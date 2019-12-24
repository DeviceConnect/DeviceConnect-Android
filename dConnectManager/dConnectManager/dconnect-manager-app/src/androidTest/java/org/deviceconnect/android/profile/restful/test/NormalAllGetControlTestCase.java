/*
 NormalAllGetControlTestCase.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * リクエストURLにmethodを指定した時に、
 * リクエストを全てGETで操作するための機能の正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalAllGetControlTestCase extends RESTfulDConnectTestCase {
    /**
     * プロファイル名: {@value} .
     */
    public static final String PROFILE_NAME = "allGetControl";

    /**
     * インターフェース名: {@value} .
     */
    private static final String INTERFACE_TEST = "test";

    /**
     * 属性名: {@value} .
     */
    private static final String ATTRIBUTE_PING = "ping";

    /**
     * パラメータ: {@value}.
     */
    private static final String PARAM_KEY = "key";

    /**
     * テスト値: {@value}.
     */
    private static final String VALUE_KEY_PROFILE = "PROFILE_OK";

    /**
     * テスト値: {@value}.
     */
    private static final String VALUE_KEY_INTERFACE = "INTERFACE_OK";

    /**
     * テスト値: {@value}.
     */
    private static final String VALUE_KEY_ATTRIBUTE = "ATTRIBUTE_OK";

    /**
     * /profileのとき、methodにGETが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testGetRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_PROFILE));
    }

    /**
     * /profile/attributeのとき、methodにGETが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testGetRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_ATTRIBUTE));
    }
    /**
     * /profile/interface/attributeのとき、methodにGETが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     */
    @Test
    public void testGetRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_INTERFACE));
    }
    /**
     * /profileのとき、methodにPOSTが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPostRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_PROFILE));
    }

    /**
     * /profile/attributeのとき、methodにPOSTが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPostRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_ATTRIBUTE));
    }
    /**
     * /profile/interface/attributeのとき、methodにPOSTが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POSt/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPostRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_INTERFACE));
    }
    /**
     * /profileのとき、methodにPUTが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPutRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_PROFILE));
    }

    /**
     * /profile/attributeのとき、methodにPUTが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPutRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_ATTRIBUTE));
    }
    /**
     * /profile/interface/attributeのとき、methodにGETが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testPutRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_INTERFACE));
    }

    /**
     * /profileのとき、methodにDELEteが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testDeleteRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_PROFILE));
    }

    /**
     * /profile/attributeのとき、methodにDELETEが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testDeleteRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_ATTRIBUTE));
    }
    /**
     * /profile/interface/attributeのとき、methodにDELETEが指定されている時でも、正常にリクエストが処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・デバイスプラグインで指定されているレスポンスがそのまま返されること
     * </pre>
     *
     */
    @Test
    public void testDeleteRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(PARAM_KEY), is(VALUE_KEY_INTERFACE));
    }
}
