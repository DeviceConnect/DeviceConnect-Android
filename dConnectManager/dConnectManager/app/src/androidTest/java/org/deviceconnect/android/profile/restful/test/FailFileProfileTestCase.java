/*
 FailFileProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.content.res.AssetManager;
import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.deviceconnect.android.test.plugin.profile.TestFileProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.FileProfileConstants.FileType;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * FileDescriptorプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailFileProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdが無い状態でファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?serviceId=
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?serviceId=123456789
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?serviceId=xxxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetListUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/list?serviceId=123456789&serviceId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/list?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/list?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイル一覧取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/list?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetListInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_LIST);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?serviceId=&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?serviceId=123456789&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?serviceId=xxxxx&mediaId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?serviceId=123456789&serviceId=xxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/receive?serviceId=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveNoMediaId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/receive?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/receive?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイル受信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/receive?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceiveInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RECEIVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?serviceId=&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?serviceId=123456789&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?serviceId=xxxxx&mediaId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(FileProfileConstants.PARAM_FILE_TYPE, String.valueOf(FileType.FILE.getValue()));
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
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
     * serviceIdを2重に指定してファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?serviceId=123456789&serviceId=xxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * media属性を指定せずにファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/send?serviceId=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendNoMedia() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/send?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("PUT", builder.toString(), null, body);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイル送信テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/send?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostSendInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=123456789&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=xxxxx&mediaId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=123456789&serviceId=xxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * pathを指定せずにファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが1で返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveNoPath() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/remove?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpGet request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/remove?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpPut request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
 
    /**
     * メソッドにPOSTを指定してファイル削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/remove?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRemoveInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_REMOVE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH,
                TestFileProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        String name = "test.png";
        byte[] data = getBytesFromAssets(name);
        if (data == null) {
            fail("Cannot find the file." + name);
        }
        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, data);
        try {
            JSONObject response = sendRequest("POST", builder.toString(), null, body);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), response);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirNoServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);            
            assertResultError(root);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?serviceId=&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirEmptyServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?serviceId=123456789&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirInvalidServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "12345678");
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義に無い属性を指定してディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?serviceId=xxxx&path=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirUndefinedAttribute() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter("abc", "abc");

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?serviceId=12345678&serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirDuplicatedServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "12345678");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/mkdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirInvalidMethodGet() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpGet request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/mkdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirInvalidMethodPut() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPut request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してディレクトリ作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/mkdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostMkdirInvalidMethodDelete() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_MKDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/rmdir?path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirNoServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/rmdir?serviceId=&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirEmptyServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/rmdir?serviceId=123456789&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirInvalidServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "12345678");
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義に無い属性を指定してディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/rmdir?serviceId=xxxx&path=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirUndefinedAttribute() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter("abc", "abc");

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file/rmdir?serviceId=12345678&serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirDuplicatedServiceId() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "12345678");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpDelete request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file/rmdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirInvalidMethodGet() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpGet request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file/rmdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirInvalidMethodPost() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPost request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してディレクトリ削除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file/rmdir?serviceId=xxxx&path=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteRmdirInvalidMethodPut() {
        final String name = "test";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_RMDIR);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileProfileConstants.PARAM_PATH, name);
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

        try {
            HttpPut request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(root);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * マルチパート送信テスト用ファイルデータを生成する.
     * 
     * @return ファイルデータを格納したマルチパート
     */
    private MultipartEntity getEntity() {
        AssetManager manager = getApplicationContext().getAssets();
        String name = "test.png";
        MultipartEntity entity = new MultipartEntity();
        try {
            entity.addPart(FileProfileConstants.PARAM_DATA, new InputStreamBody(manager.open(name), name));
        } catch (IOException e) {
            fail("Failed to obtain the file: " + e.getMessage());
        }
        return entity;
    }
}
