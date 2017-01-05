/*
 FailFileDescriptorProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestFileDescriptorProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * FileDescriptorプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailFileDescriptorProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?serviceId=&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?serviceId=123456789&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?serviceId=xxxxx&path=xxxx&flag=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter("abc", "abc");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 必須パラメータpathが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?serviceId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoPath() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 必須パラメータflagが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/open?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoFlag() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }


    /**
     * メソッドにPOSTを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/open?serviceId=xxxx&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPUTを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/open?serviceId=xxxx&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/open?serviceId=xxxx&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseNoServiceID() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?serviceId=&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?serviceId=123456789&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?serviceId=xxxxx&path=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにGETを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/close?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/close?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?serviceId=&path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?serviceId=123456789&path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?serviceId=xxxxx&path=xxxx&length=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_LENGTH, "1");
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 必須パラメータpathを指定せずにファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/read?serviceId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoPath() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_LENGTH,
                String.valueOf(TestFileDescriptorProfileConstants.BYTE));
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 必須パラメータlengthを指定せずにファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoLength() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPUTを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?path=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteNoServiceID() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=&path=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=123456789&mediId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=xxxxx&path=xxxx&abc=abc
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        Map<String, Object> body = new HashMap<>();
        body.put(FileDescriptorProfileConstants.PARAM_DATA, "test".getBytes());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにGETを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=xxxx&path=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/write?serviceId=xxxx&path=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/write?serviceId=xxxx&path=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/onWatchFile
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/onWatchFile?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/onWatchFile?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにPOSTを指定してonWatchFile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/onWatchFile
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/onWatchFile?serviceId=&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/onWatchFile?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定してonWatchFile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにPOSTを指定してonWatchFile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
