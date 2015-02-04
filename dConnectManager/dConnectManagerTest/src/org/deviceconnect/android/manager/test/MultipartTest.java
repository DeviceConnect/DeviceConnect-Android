/*
 MultipartTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
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


/**
 * リクエストのマルチパート解析処理のテスト.
 * @author NTT DOCOMO, INC.
 */
public class MultipartTest extends RESTfulDConnectTestCase {

    /**
     * コンストラクタ.
     * 
     * @param tag テストタグ
     */
    public MultipartTest(final String tag) {
        super(tag);
    }

    /**
     * POSTリクエストパラメータをマルチパートで指定可能であることのテスト.
     */
    public void testParsingMutilpartAsRequestParametersMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(NotificationProfileConstants.PROFILE_NAME);
        builder.setAttribute(NotificationProfileConstants.ATTRIBUTE_NOTIFY);
        try {
            MultipartEntity entity = new MultipartEntity();
            entity.addPart(DConnectProfileConstants.PARAM_SERVICE_ID, new StringBody(getServiceId()));
            entity.addPart(NotificationProfileConstants.PARAM_TYPE, new StringBody("0"));
            entity.addPart(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, new StringBody(getAccessToken()));
            HttpPost request = new HttpPost(builder.toString());
            request.setEntity(entity);
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    /**
     * PUTリクエストパラメータをマルチパートで指定可能であることのテスト.
     */
    public void testParsingMutilpartAsRequestParametersMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
        builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        try {
            MultipartEntity entity = new MultipartEntity();
            entity.addPart(DConnectProfileConstants.PARAM_SERVICE_ID, new StringBody(getServiceId()));
            entity.addPart(DConnectProfileConstants.PARAM_SESSION_KEY, new StringBody(getClientId()));
            entity.addPart(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, new StringBody(getAccessToken()));
            HttpPut request = new HttpPut(builder.toString());
            request.setEntity(entity);
            JSONObject response = sendRequest(request);
            assertResultOK(response);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
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
            MultipartEntity entity = new MultipartEntity();
            // ボディに0バイトのデータを追加
            entity.addPart(FileProfileConstants.PARAM_DATA,
                    new BinaryBody(new byte[] {}, "zero.dat"));

            HttpPost request = new HttpPost(builder.toString());
            request.setEntity(entity);

            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
