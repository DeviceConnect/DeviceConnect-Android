/*
 NormalSettingsProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestSettingsProfileConstants;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.json.JSONException;
import org.json.JSONObject;



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
     * Path: /settings/volume?deviceid=xxxx&kind=1
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?deviceid=xxxx&kind=2
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?deviceid=xxxx&kind=3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?deviceid=xxxx&kind=4
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/volume?deviceid=xxxx&kind=5
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?deviceid=xxxx&kind=1&level=xxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?deviceid=xxxx&kind=2&level=xxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?deviceid=xxxx&kind=3&level=xxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?deviceid=xxxx&kind=4&level=xxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/volume?deviceid=xxxx&kind=5&level=xxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの日時取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?deviceid=xxxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.DATE, 
                    resp.getString(SettingsProfileConstants.PARAM_DATE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスの日時設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?deviceid=xxxx&date=xxxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(TestSettingsProfileConstants.LEVEL, 
                    resp.getDouble(SettingsProfileConstants.PARAM_LEVEL));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?deviceid=xxxx&level=xxxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
            assertEquals(1, 
                    resp.getInt(SettingsProfileConstants.PARAM_TIME));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?deviceid=xxxx&kind=1&level=xxxx
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
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject resp = sendRequest(request);
            assertResultOK(resp);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
