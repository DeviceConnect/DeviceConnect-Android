/*
 MultipartTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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


/**
 * リクエストのマルチパート解析処理のテスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class MultipartTest extends RESTfulDConnectTestCase {
    /**
     * POSTリクエストパラメータをマルチパートで指定可能であることのテスト.
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        try {
            MultipartEntityBuilder build = MultipartEntityBuilder.create();
            build.addTextBody(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
            build.addTextBody(NotificationProfileConstants.PARAM_TYPE, "0");
            build.addTextBody(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
            HttpPost request = new HttpPost(builder.toString());
            request.setEntity(build.build());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    /**
     * PUTリクエストパラメータをマルチパートで指定可能であることのテスト.
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        try {
            MultipartEntityBuilder build = MultipartEntityBuilder.create();
            build.addTextBody(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
            build.addTextBody(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
            build.addTextBody(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
            HttpPut request = new HttpPut(builder.toString());
            request.setEntity(build.build());
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (JSONException e) {
            fail(e.getMessage());
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
        try {
            MultipartEntityBuilder build = MultipartEntityBuilder.create();
            build.addBinaryBody(FileProfileConstants.PARAM_DATA, new byte[0]);
            HttpPost request = new HttpPost(builder.toString());
            request.setEntity(build.build());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
