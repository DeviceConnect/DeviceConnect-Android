/*
 FailSettingsProfileTestCase.java
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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestSettingsProfileConstants;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Settingsプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailSettingsProfileTestCase extends RESTfulDConnectTestCase
    implements TestSettingsProfileConstants {

    /**
     * serviceIdを指定せずに音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/sound/volume?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSoundVolumeNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態で音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/sound/volume?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSoundVolumeEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdで音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/sound/volume?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSoundVolumeInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定して音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/sound/volume?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSoundVolumeDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずに音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/sound/volume?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態で音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/sound/volume?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdで音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/sound/volume?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定して音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/sound/volume?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定して音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /settings/sound/volume?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定して音量取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /settings/sound/volume?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSoundVolumeInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_SOUND);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_KIND, String.valueOf(VOLUME_KIND));
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずに日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDateNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
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
     * serviceIdが空状態で日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDateEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdで日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDateInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定して日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/date?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDateDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずに日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態で日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdで日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定して日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/date?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定して日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /settings/date?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定して日時取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /settings/date?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDateInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_DATE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_DATE, DATE);
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplayLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
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
     * serviceIdが空状態でバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplayLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplayLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/light?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplayLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/light?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /settings/display/light?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /settings/display/light?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplayLightInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_LIGHT);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_LEVEL, String.valueOf(LEVEL));
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplaySleepNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
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
     * serviceIdが空状態でバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplaySleepEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplaySleepInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /settings/display/sleep?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetDisplaySleepDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを指定せずにバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepNoServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.EMPTY_SERVICE_ID.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdが空状態でバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?serviceId=&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepEmptyServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "");
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 存在しないserviceIdでバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?serviceId=123456789&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepInvalidServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * serviceIdを2重に指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /settings/display/sleep?serviceId=123456789&serviceId=xxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・先に定義された属性が優先されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepDuplicatedServiceId() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, "123456789");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_FOUND_SERVICE.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにPOSTを指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /settings/display/sleep?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepInvalidMethodPost() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * メソッドにDELETEを指定してバックライト明度取得要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /settings/display/sleep?serviceId=xxxx&kind=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDisplaySleepInvalidMethodDelete() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SettingsProfileConstants.PROFILE_NAME);
        builder.setInterface(SettingsProfileConstants.INTERFACE_DISPLAY);
        builder.setAttribute(SettingsProfileConstants.ATTRIBUTE_SLEEP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(SettingsProfileConstants.PARAM_TIME, String.valueOf(TIME));
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultError(ErrorCode.NOT_SUPPORT_ACTION.getCode(), root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
