/*
 NormalFileDescriptorProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.FileDescriptorProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


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
     * Path: /fileDescriptor/open?serviceId=xxxx&path=xxxx&flag=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
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

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ファイルをクローズするテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/close?serviceId=xxxx&path=xxxx
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

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx&length=xxxx
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

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getInt(FileDescriptorProfile.PARAM_SIZE), is(notNullValue()));
        assertThat(response.getString(FileDescriptorProfile.PARAM_FILE_DATA), is(notNullValue()));
    }

    /**
     * 指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/read?serviceId=xxxx&path=xxxx&length=xxxx&position=xxxx
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

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getInt(FileDescriptorProfile.PARAM_SIZE), is(notNullValue()));
        assertThat(response.getString(FileDescriptorProfile.PARAM_FILE_DATA), is(notNullValue()));
    }

    /**
     * ファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=xxxx&path=xxxx
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
        body.put(FileDescriptorProfile.PARAM_DATA, "test".getBytes());

        DConnectResponseMessage response = sendRequest("PUT", builder.toString(), null, body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ファイルに書き込むテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/write?serviceId=xxxx&mediaId=xxxx&position=2
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
        body.put(FileDescriptorProfile.PARAM_DATA, "test".getBytes());

        DConnectResponseMessage response = sendRequest("PUT", builder.toString(), null, body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メソッドにGETを指定してonWatchFile属性のリクエストテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx&sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOnWatchFile() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(FileDescriptorProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ファイルの更新通知のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx
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
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = sendRequest("PUT", builder.toString(), null, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ファイルの更新通知のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /fileDescriptor/onWatchFile?serviceId=xxxx
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
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = sendRequest("DELETE", builder.toString(), null, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
