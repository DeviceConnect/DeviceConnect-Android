/*
 FailAllGetControlTestCase.java
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
 * リクエストを全てGETで操作するための機能の異常系テスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailAllGetControlTestCase extends RESTfulDConnectTestCase {
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

    // Http Method post test.

    /**
     * HTTPメソッドがPOSTで、/profileのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /GET/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostGetRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profile/attributeのとき、methodにGETが指定されている時に、エラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /GET/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostGetRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /GET/allGetControl/test/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostGetRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profileのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /POST/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPostRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、 /profile/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /POST/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPostRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profile/interface/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /POST/allGetControl/test/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPostRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profileのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /PUT/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPutRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }


    /**
     * HTTPメソッドがPOSTで、/profile/attributeのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /PUT/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPutRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、 /profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /PUT/allGetControl/test/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostPutRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、 /profileのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /DELETE/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostDeleteRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profile/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /DELETE/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostDeleteRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPOSTで、/profile/interface/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /DELETE/allGetControl/test/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPostDeleteRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    // Http Method put test.

    /**
     * HTTPメソッドがPUTで、/profileのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /GET/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutGetRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/attributeのとき、methodにGETが指定されている時に、エラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /GET/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutGetRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /GET/allGetControl/test/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutGetRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profileのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /POST/allGetControl?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPostRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、 /profile/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /POST/allGetControl/ping?serviceId&amp;amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPostRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/interface/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /POST/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPostRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profileのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /PUT/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPutRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/attributeのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /PUT/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPutRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、 /profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /PUT/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutPutRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、 /profileのとき、methodにDELEteが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /DELETE/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutDeleteRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /DELETE/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutDeleteRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        putInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがPUTで、/profile/interface/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /DELETE/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodPutDeleteRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        postInvalidUrl(builder);
    }

    // Http Method delete test.

    /**
     * HTTPメソッドがDELETEで、/profileのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /GET/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteGetRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profile/attributeのとき、methodにGETが指定されている時に、エラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /GET/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteGetRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /GET/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteGetRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profileのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /POST/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePostRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、 /profile/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /POST/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePostRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profile/interface/attributeのとき、methodにPOSTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /POST/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePostRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profileのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /PUT/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePutRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }


    /**
     * HTTPメソッドがDELETEで、/profile/attributeのとき、methodにPUTが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /PUT/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePutRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、 /profile/interface/attributeのとき、methodにGETが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /PUT/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeletePutRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、 /profileのとき、methodにDELEteが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /DELETE/allGetControl?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteDeleteRequestProfile() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profile/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /DELETE/allGetControl/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteDeleteRequestProfileAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /**
     * HTTPメソッドがDELETEで、/profile/interface/attributeのとき、methodにDELETEが指定されている時にエラー処理されること.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /DELETE/allGetControl/test/ping?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid urlエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testHttpMethodDeleteDeleteRequestProfileInterfaceAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(PROFILE_NAME);
        builder.append("/").append(INTERFACE_TEST);
        builder.append("/").append(ATTRIBUTE_PING);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        deleteInvalidUrl(builder);
    }

    /** ProfileにHttpメソッドが指定されている. */
    /**
     * methodが指定されていない時、profile名にGETが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodGetByNormal() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodが指定されていない時、profile名にPOSTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPostByNormal() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodが指定されていない時、profile名にPUTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPutByNormal() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodが指定されていない時、profile名にDELETEが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodDeleteByNormal() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    // Method指定時にProfileにHttpメソッドが指定されている.

    /**
     * methodがGETで指定されている時、profile名にGETが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/GET?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodGetGetByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }


    /**
     * methodがGETで指定されている時、profile名にGETが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/POST?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodGetPostByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にGETが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/PUT?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodGetPutByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にGETが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /GET/DELETE?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodGetDeleteByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPOSTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/GET?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPostGetByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPOSTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/POST?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPostPostByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPOSTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/PUT?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPostPutByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPOSTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /POST/DELETE?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPostDeleteByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPUTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/GET?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPutGetByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPUTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/POST?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPutPostByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPUTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/PUT?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPutPutByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にPUTが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /PUT/DELETE?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodPutDeleteByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にDELETEが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/GET?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodDeleteGetByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(DConnectMessage.METHOD_GET);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にDELETEが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/POST?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodDeletePostByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(DConnectMessage.METHOD_POST);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にDELETEが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/PUT?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodDeletePutByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(DConnectMessage.METHOD_PUT);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    /**
     * methodがGETで指定されている時、profile名にDELETEが指定されている場合はエラー処理する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /DELETE/DELETE?serviceId&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * ・Invalid profileエラーが返って来ること。
     * </pre>
     */
    @Test
    public void testProfileHttpMethodDeleteByAllGetControl() {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("/").append(DConnectMessage.METHOD_DELETE);
        builder.append("?").append(DConnectProfileConstants.PARAM_SERVICE_ID).append("=").append(getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN).append("=").append(getAccessToken());

        getInvalidProfile(builder);
    }

    // private method

    /**
     * HttpMethodがPostの状態で全てGETで操作するときのメソッドを投げた場合,
     * Invalid Urlが返って来る時のテスト用メソッド.
     *
     * @param builder URL
     */
    private void postInvalidUrl(StringBuilder builder) {
        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getInt(DConnectMessage.EXTRA_ERROR_CODE),
                is(DConnectMessage.ErrorCode.INVALID_URL.getCode()));
    }

    /**
     * HttpMethodがPutの状態で全てGETで操作するときのメソッドを投げた場合,
     * Invalid Urlが返って来る時のテスト用メソッド.
     *
     * @param builder URL
     */
    private void putInvalidUrl(StringBuilder builder) {
        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getInt(DConnectMessage.EXTRA_ERROR_CODE),
                is(DConnectMessage.ErrorCode.INVALID_URL.getCode()));
    }

    /**
     * HttpMethodがDeleteの状態で全てGETで操作するときのメソッドを投げた場合,
     * Invalid Urlが返って来る時のテスト用メソッド.
     *
     * @param builder URL
     */
    private void deleteInvalidUrl(StringBuilder builder) {
        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getInt(DConnectMessage.EXTRA_ERROR_CODE),
                is(DConnectMessage.ErrorCode.INVALID_URL.getCode()));
    }

    /**
     * HttpMethodがGetの状態で全てGETで操作するときのメソッドを投げた場合,
     * Invalid Profileが返って来る時のテスト用メソッド.
     *
     * @param builder URL
     */
    private void getInvalidProfile(StringBuilder builder) {
        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getInt(DConnectMessage.EXTRA_ERROR_CODE),
                is(DConnectMessage.ErrorCode.INVALID_PROFILE.getCode()));
    }
}
