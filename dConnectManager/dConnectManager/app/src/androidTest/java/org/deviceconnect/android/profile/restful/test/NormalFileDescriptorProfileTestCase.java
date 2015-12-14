/*
 NormalFileDescriptorProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestFileDescriptorProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;


/**
 * FileDescriptorプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalFileDescriptorProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * ファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?deviceid=xxxx&mediaid=xxxx&flag=xxxx&mode=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・mediaidに"test.txt"が返ってくること。
     * </pre>
     */
    @Test
    public void testOpen() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_FLAG + "=r");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK, root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?deviceid=xxxx&mediaid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testClose() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK, root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?deviceid=xxxx&mediaid=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testRead001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_LENGTH + "=256");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            assertResultOK(root);
            Assert.assertEquals(TestFileDescriptorProfileConstants.BYTE,
                    root.getInt(FileDescriptorProfileConstants.PARAM_SIZE));
            Assert.assertEquals(TestFileDescriptorProfileConstants.FILE_DATA, 
                    root.getString(FileDescriptorProfileConstants.PARAM_FILE_DATA));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?deviceid=xxxx&mediaid=xxxx&length=xxxx&position=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testRead002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_LENGTH + "=256");
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_POSITION + "=0");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK, root.getInt(DConnectMessage.EXTRA_RESULT));
            Assert.assertEquals(TestFileDescriptorProfileConstants.BYTE,
                    root.getInt(FileDescriptorProfileConstants.PARAM_SIZE));
            Assert.assertEquals(TestFileDescriptorProfileConstants.FILE_DATA, 
                    root.getString(FileDescriptorProfileConstants.PARAM_FILE_DATA));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?deviceid=xxxx&mediaid=xxxx
     * Entity: 文字列"test"。
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testWrite001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        Map<String, Object> body = new HashMap<>();
        body.put("media", "test".getBytes());
        try {
            JSONObject response = sendRequest("PUT", builder.toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?deviceid=xxxx&mediaid=xxxx&position=2
     * Entity: 文字列"test"。
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testWrite002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_PATH + "=test.txt");
        builder.append("&");
        builder.append(FileDescriptorProfileConstants.PARAM_POSITION + "=2");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        Map<String, Object> body = new HashMap<>();
        body.put("media", "test".getBytes());
        try {
            JSONObject response = sendRequest("PUT", builder.toString(), null, body);
            assertResultOK(response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してonwatchfile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/onwatchfile?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOnWatchFile() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK, root.getInt(DConnectMessage.EXTRA_RESULT));
            JSONObject file = root.getJSONObject(FileDescriptorProfileConstants.PARAM_FILE);
            Assert.assertEquals(TestFileDescriptorProfileConstants.PATH, 
                    file.getString(FileDescriptorProfileConstants.PARAM_PATH));
            Assert.assertEquals(TestFileDescriptorProfileConstants.CURR, 
                    file.getString(FileDescriptorProfileConstants.PARAM_CURR));
            Assert.assertEquals(TestFileDescriptorProfileConstants.PREV, 
                    file.getString(FileDescriptorProfileConstants.PARAM_PREV));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルの更新通知のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testWatchFile01() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpPut request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            Assert.assertNotNull("root is null.", root);
            Assert.assertEquals(DConnectMessage.RESULT_OK, root.getInt(DConnectMessage.EXTRA_RESULT));
            JSONObject response = waitForEvent();
            JSONObject file = response.getJSONObject(FileDescriptorProfileConstants.PARAM_FILE);
            Assert.assertEquals(TestFileDescriptorProfileConstants.PATH, 
                    file.getString(FileDescriptorProfileConstants.PARAM_PATH));
            Assert.assertEquals(TestFileDescriptorProfileConstants.CURR, 
                    file.getString(FileDescriptorProfileConstants.PARAM_CURR));
            Assert.assertEquals(TestFileDescriptorProfileConstants.PREV, 
                    file.getString(FileDescriptorProfileConstants.PARAM_PREV));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * ファイルの更新通知のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?deviceid=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testWatchFile02() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + FileDescriptorProfileConstants.PROFILE_NAME);
        builder.append("/" + FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
