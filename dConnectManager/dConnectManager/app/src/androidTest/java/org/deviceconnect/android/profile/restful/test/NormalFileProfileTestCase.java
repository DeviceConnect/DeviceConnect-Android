/*
 NormalFileProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Fileプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalFileProfileTestCase extends RESTfulDConnectTestCase {
    /** バッファサイズを定義. */
    private static final int BUF_SIZE = 4096;

    /**
     * ファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetList001() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?deviceid=xxxx&mimeType=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・
     * </pre>
     */
    @Test
    public void testGetList002() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_MIME_TYPE, "text/plain");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?deviceid=xxxx&mediaid=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceive() {
        testSend();
        final String name = "test.png";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, "/test/" + name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);

            String uri = root.getString(FileProfileConstants.PARAM_URI);
            Assert.assertNotNull("uri is null.", uri);
            uri += "&" + AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken();
            byte[] data = getBytesFromHttp(uri);
            byte[] orig = getBytesFromAssets(name);
            Assert.assertNotNull("data is invalid.", data);
            Assert.assertEquals("length of data is invalid", orig.length, data.length);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルの送信を行う.
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
    public void testSend() {
        final String name = "test.png";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(FileProfileConstants.PARAM_PATH, "/test/test.png");
        builder.addParameter(FileProfileConstants.PARAM_FILE_TYPE,
                String.valueOf(FileProfileConstants.FileType.FILE.getValue()));

        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルの削除を行う.
     * <pre>
     * Method: Delete
     * Path: /file/remove?deviceid=xxxx&filename=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testRemove() {
        final String name = "test.png";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ディレクトリの作成および削除を行う.
     * <pre>
     * Method: DELETE
     * Path: /file/rmdir?deviceid=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testMkdirRmdir01() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }

        builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ディレクトリの作成および削除を行う.
     * <pre>
     * Method: DELETE
     * Path: /file/rmdir?deviceid=xxxx&path=xxxx&force=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testMkdirRmdir02() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }

        builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(FileProfileConstants.PARAM_FORCE, "true");
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
