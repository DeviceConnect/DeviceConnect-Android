/*
 FailServiceInformationProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Service Informationプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailServiceInformationProfileTestCase extends RESTfulDConnectTestCase {
    /**
     * serviceIdを指定せずにサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceInformation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdに空文字を指定してサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceInformation?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdを指定してサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceInformation?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 未定義のパラメータを指定してサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceInformation?serviceId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・未定義のパラメータは無視されること。
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationUndefinedParameter() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter("abc", "abc");

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * POSTメソッドでサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /serviceInformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * PUTメソッドでサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /serviceInformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * DELETEメソッドでサービス情報を取得する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /serviceInformation?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServiceInformationInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
