/*
 MultipartTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * リクエストのマルチパート解析処理のテスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class MultipartTest extends RESTfulDConnectTestCase {
    /**
     * POSTリクエストパラメータをマルチパートで指定可能であることのテスト.
     * <pre>
     * Method: POST
     * Path: /notification/notify
     * Body: serviceId=xxxx&type=0&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);

        Map<String, Object> body = new HashMap<>();
        body.put(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        body.put(NotificationProfileConstants.PARAM_TYPE, "0");
        body.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = sendRequest("POST", builder.build().toString(), null, body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * PUTリクエストパラメータをマルチパートで指定可能であることのテスト.
     * <pre>
     * Method: PUT
     * Path: /deviceOrientation/onDeviceOrientation
     * Body: serviceId=xxxx&sessionKey=xxxx&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        Map<String, Object> body = new HashMap<>();
        body.put(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        body.put(DConnectProfileConstants.PARAM_SESSION_KEY, "clientId");
        body.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = sendRequest("PUT", builder.build().toString(), null, body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 0バイトのファイルも送信可能であることのテスト.
     * <pre>
     * Method: POST
     * Path: /file/send?serviceId=xxxx&filename=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testSendZeroByteFile() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(FileProfileConstants.PARAM_PATH, "/test/zero.dat");
        builder.addParameter(FileProfileConstants.PARAM_FILE_TYPE,
                String.valueOf(FileProfileConstants.FileType.FILE.getValue()));

        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, new byte[0]);

        DConnectResponseMessage response = sendRequest("POST", builder.build().toString(), null, body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
