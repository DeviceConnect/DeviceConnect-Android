/*
 FailPhoneProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestPhoneProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Phoneプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailPhoneProfileTestCase extends RESTfulDConnectTestCase
    implements TestPhoneProfileConstants {

    /**
     * serviceIdを指定せずに通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態で通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?serviceId=&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdで通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?serviceId=123456789&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * phoneNumberを指定せずに通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallNoPhoneNumber() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにGETを指定して通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /phone/call?serviceId=xxxx&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPUTを指定して通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/call?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定して通話発信要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/call?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostCallInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_PHONE_NUMBER, PHONE_NUMBER);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * serviceIdを指定せずに電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/call?phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態で電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/call?serviceId=&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdで電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/call?serviceId=123456789&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * modeを指定せずに電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/set?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetNoMode() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_SET);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.INVALID_REQUEST_PARAMETER.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにGETを指定して電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /phone/call?serviceId=xxxx&phoneNumber=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定して電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_SET);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにDELETEを指定して電話設定要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/call?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSetInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(PhoneProfileConstants.PARAM_MODE, String.valueOf(MODE));

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * serviceIdが無い状態でonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/onConnect?
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnConnectNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/onConnect?serviceId=&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnConnectEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/onConnect?serviceId=123456789&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnConnectInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/onConnect?
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnConnectNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/onConnect?serviceId=&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnConnectEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonConnect属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/onConnect?serviceId=123456789&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnConnectInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してonConnect属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/onConnect?serviceId=xxxx&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnConnectInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(PhoneProfileConstants.PROFILE_NAME);
        builder.setAttribute(PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
