/*
 MultipartTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.android.profile.restful.test.TestURIBuilder;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


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
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);

        Map<String, Object> body = new HashMap<>();
        body.put(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        body.put(NotificationProfileConstants.PARAM_TYPE, "0");
        body.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            JSONObject response = sendRequest("POST", builder.build().toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Failed to parse JSON: " + e.getMessage());
        } catch (URISyntaxException e) {
            fail("Failed to create uri: " + e.getMessage());
        }
    }

    /**
     * PUTリクエストパラメータをマルチパートで指定可能であることのテスト.
     * <pre>
     * Method: PUT
     * Path: /deviceorientation/ondeviceorientation
     * Body: serviceId=xxxx&sessionKey=xxxx&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        Map<String, Object> body = new HashMap<>();
        body.put(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        body.put(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
        body.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            JSONObject response = sendRequest("PUT", builder.build().toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Failed to parse JSON: " + e.getMessage());
        } catch (URISyntaxException e) {
            fail("Failed to create uri: " + e.getMessage());
        }
    }

    /**
     * 0バイトのファイルも送信可能であることのテスト.
     * <pre>
     * Method: POST
     * Path: /file/send?deviceid=xxxx&filename=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testSendZeroByteFile() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(FileProfileConstants.PARAM_PATH, "/test/zero.dat");
        builder.addParameter(FileProfileConstants.PARAM_FILE_TYPE,
                String.valueOf(FileProfileConstants.FileType.FILE.getValue()));

        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, new byte[0]);
        try {
            JSONObject response = sendRequest("POST", builder.build().toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Failed to parse JSON: " + e.getMessage());
        } catch (URISyntaxException e) {
            fail("Failed to create uri: " + e.getMessage());
        }
    }
}
