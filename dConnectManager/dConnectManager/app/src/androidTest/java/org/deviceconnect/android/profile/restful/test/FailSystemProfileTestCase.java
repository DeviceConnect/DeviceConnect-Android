/*
 FailSystemProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.SystemProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Systemプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailSystemProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * 未定義のパラメータを指定してシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system?abc=abc
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・versionにString型の値が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemUndefinedParameter() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.addParameter("abc", "abc");

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * POSTメソッドでシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * PUTメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * DELETEメソッドでデバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device?serviceId=123456789&serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * GETメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/events
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceEventsInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * POSTメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/events
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceEventsInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * PUTメソッドでイベント全消去要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /system/device/events
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceEventsInvalidMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * GETメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceKeywordInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * POSTメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceKeywordInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * DELETEメソッドでキーワード表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device/keyword
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceKeywordInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_KEYWORD);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * GETメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceWakeupInvalidMethodGet() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * POSTメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceWakeupInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
    
    /**
     * DELETEメソッドで設定画面表示要求を送信する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/device/wakeup?pluginId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDeviceWakeupInvalidMethodDelete() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);
        builder.addParameter(SystemProfileConstants.PARAM_PLUGIN_ID, getTestPluginId());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.NOT_SUPPORT_ACTION.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
