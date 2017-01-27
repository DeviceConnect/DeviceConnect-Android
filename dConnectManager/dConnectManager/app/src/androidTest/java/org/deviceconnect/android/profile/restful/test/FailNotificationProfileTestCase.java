/*
 FailNotificationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
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
import org.deviceconnect.profile.NotificationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Notificationプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailNotificationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdを指定せずに通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/notify?type=0
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態で通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/notify?serviceId=&type=0
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdで通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/notify?serviceId=123456789&type=0
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定して通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/notify?serviceId=xxxx&type=0&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにGETを指定して通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /notification/notify?serviceId=xxxx&type=0
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPUTを指定して通知を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/notify?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostNotifyInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdを指定せずに通知を削除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/notify?notificationId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNotifyNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態で通知を削除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/notify?serviceId=&notificationId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNotifyEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdで通知を削除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/notify?serviceId=123456789&notificationId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNotifyInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(NotificationProfileConstants.PARAM_TYPE, "0");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 定義にない属性を指定して通知を削除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/notify?serviceId=xxxx&notificationId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteNotifyUndefinedAttribute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(NotificationProfileConstants.PARAM_NOTIFICATION_ID, "0");
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * serviceIdが無い状態でonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClick
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnClickChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClick?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnClickChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClick?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnClickChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClick
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnClickChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClick?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnClickChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonClick属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClick?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnClickChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してonClick属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/onClick?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnClickChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLICK);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onShow
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnShowChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onShow?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnShowChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onShow?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnShowChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onShow?
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnShowChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onShow?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnShowChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonShow属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onShow?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnShowChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してonShow属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/onShow?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnShowChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_SHOW);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClose
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnCloseChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClose?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnCloseChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onClose?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnCloseChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClose
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnCloseChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClose?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnCloseChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonClose属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onClose?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnCloseChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してonClose属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/onClose?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnCloseChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onError?
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnErrorChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onError?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnErrorChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /notification/onError?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnErrorChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが無い状態でonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onError
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnErrorChangeNoServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.EMPTY_SERVICE_ID.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdが空状態でonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onError?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnErrorChangeEmptyServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * 存在しないserviceIdでonError属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /notification/onError?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnErrorChangeInvalidServiceId() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_FOUND_SERVICE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * メソッドにPOSTを指定してonError属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /notification/onError?serviceId=xxxx&
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnErrorChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_ON_ERROR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
