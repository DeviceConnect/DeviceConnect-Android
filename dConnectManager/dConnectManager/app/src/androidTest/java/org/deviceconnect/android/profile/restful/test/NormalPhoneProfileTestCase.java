/*
 NormalPhoneProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestPhoneProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Phoneプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalPhoneProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * 電話発信要求テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /phone/call?serviceId=xxxx&mediaid=yyyy
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPostCall() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_CALL);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(PhoneProfileConstants.PARAM_PHONE_NUMBER + "=" + TestPhoneProfileConstants.PHONE_NUMBER);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 電話に関する設定項目(サイレント・マナー・音あり)の設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/set?serviceId=xxxx&mode=0
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutSet001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_SET);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(PhoneProfileConstants.PARAM_MODE + "=0");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 電話に関する設定項目(マナー)の設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/set?serviceId=xxxx&mode=1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutSet002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_SET);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(PhoneProfileConstants.PARAM_MODE + "=1");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 電話に関する設定項目(音あり)の設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/set?serviceId=xxxx&mode=2
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutSet003() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_SET);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(PhoneProfileConstants.PARAM_MODE + "=2");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 通話関連イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /phone/onconnect?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnConnect() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 通話関連イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /phone/onconnect?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnConnect() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + PhoneProfileConstants.PROFILE_NAME);
        builder.append("/" + PhoneProfileConstants.ATTRIBUTE_ON_CONNECT);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

}
