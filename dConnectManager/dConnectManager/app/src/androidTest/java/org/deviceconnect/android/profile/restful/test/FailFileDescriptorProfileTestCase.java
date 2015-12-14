/*
 FailFileDescriptorProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestFileDescriptorProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * FileDescriptorプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailFileDescriptorProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * serviceIdが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * serviceIdが空状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * 存在しないserviceIdでファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=123456789&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * 定義にない属性を指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=xxxxx&mediaId=xxxx&flag=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter("abc", "abc");
        
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * serviceIdを2重に指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=123456789&serviceId=xxx&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * 必須パラメータmediaIdが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoPath() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 必須パラメータflagが無い状態でファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/open?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenNoFlag() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }


    /**
     * メソッドにPOSTを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/open?serviceId=xxxx&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/open?serviceId=xxxx&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイルをオープンするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/open?serviceId=xxxx&mediaId=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOpenInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_FLAG, "r");
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
     * serviceIdが無い状態でファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseNoServiceID() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?serviceId=&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?serviceId=123456789&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?serviceId=xxxxx&mediaId=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?serviceId=123456789&serviceId=xxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/close?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/close?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/close?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutCloseInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * serviceIdが無い状態でファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * serviceIdが空状態でファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?serviceId=&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * 存在しないserviceIdでファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?serviceId=123456789&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * 定義にない属性を指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?serviceId=xxxxx&mediaId=xxxx&length=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_LENGTH, "1");
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
     * serviceIdを2重に指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /file_descriptor/read?serviceId=123456789&serviceId=xxx&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * 必須パラメータmediaIdを指定せずにファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/read?serviceId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoPath() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_LENGTH,
                String.valueOf(TestFileDescriptorProfileConstants.BYTE));
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 必須パラメータlengthを指定せずにファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/read?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadNoLength() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.INVALID_REQUEST_PARAMETER.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/read?serviceId=xxxx&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPUTを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/read?serviceId=xxxx&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodPut() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/read?serviceId=xxxx&mediaId=xxxx&length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReadInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_READ);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * serviceIdが無い状態でファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteNoServiceID() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?serviceId=&mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?serviceId=123456789&mediId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?serviceId=xxxxx&mediaId=xxxx&abc=abc
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        Map<String, Object> body = new HashMap<>();
        body.put("media", "test".getBytes());
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
     * serviceIdを2重に指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?serviceId=123456789&serviceId=xxx&mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにGETを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/write?serviceId=xxxx&mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodGet() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.UNKNOWN_ATTRIBUTE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/write?serviceId=xxxx&mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/write?serviceId=xxxx&mediaId=xxxx
     * Multipart: media
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutWriteInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(FileDescriptorProfileConstants.PARAM_PATH,
                TestFileDescriptorProfileConstants.PATH);
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
     * serviceIdが無い状態でonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?serviceId=&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?serviceId=123456789&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?devicdId=xxxxx&sessionKey=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・chargingがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /file_descriptor/onwatchfile?serviceId=123456789&serviceId=xxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してonwatchfile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/onwatchfile?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnWatchFileInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが無い状態でonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?serviceId=&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?serviceId=123456789&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 定義にない属性を指定してonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?devicdId=xxxxx&sessionKey=xxxx&abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・定義にない属性は無視されること。
     * ・resultが0で返ってくること。
     * ・chargingがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileUndefinedAttribute() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter("abc", "abc");
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してonwatchfile属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /file_descriptor/onwatchfile?serviceId=123456789&serviceId=xxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してonwatchfile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /file_descriptor/onwatchfile?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnWatchFileInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, TEST_SESSION_KEY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
