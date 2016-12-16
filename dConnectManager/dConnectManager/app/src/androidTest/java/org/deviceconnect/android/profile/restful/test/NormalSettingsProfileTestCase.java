/*
 NormalSettingsProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.SettingsProfile;
import org.deviceconnect.android.test.plugin.profile.TestSettingsProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Settingsプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalSettingsProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?serviceId=xxxx&kind=1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=1");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?serviceId=xxxx&kind=2
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=2");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?serviceId=xxxx&kind=3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume003() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=3");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?serviceId=xxxx&kind=4
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume004() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=4");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?serviceId=xxxx&kind=5
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume005() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=5");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?serviceId=xxxx&kind=1&level=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=1");
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?serviceId=xxxx&kind=2&level=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=2");
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?serviceId=xxxx&kind=3&level=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume003() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=3");
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?serviceId=xxxx&kind=4&level=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume004() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=4");
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?serviceId=xxxx&kind=5&level=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume005() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_SOUND);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_KIND + "=5");
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスの日時取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・dateが"2014-01-01T01:01:01+09:00"で返ってくること。
     * </pre>
     */
    @Test
    public void testGetDate() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(SettingsProfile.PARAM_DATE), is("2014-01-01T01:01:01+09:00"));
    }

    /**
     * スマートデバイスの日時設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?serviceId=xxxx&date=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDate() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_DATE + "=" + TestSettingsProfileConstants.DATE);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplayLight() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?serviceId=xxxx&level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLight() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_LEVEL + "=" + TestSettingsProfileConstants.LEVEL);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplaySleep() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getFloat(SettingsProfile.PARAM_LEVEL), is(0.5f));
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?serviceId=xxxx&kind=1&level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleep() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SettingsProfileConstants.PROFILE_NAME);
        builder.append("/" + SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.append("/" + SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(SettingsProfileConstants.PARAM_TIME + "=1");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

}
