/*
 NormalCommonTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * プロファイル共通の正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalCommonTestCase extends RESTfulDConnectTestCase {

    /**
     * URLエンコード済みの予約文字がリクエストパラメータ値に含まれている場合も、正常にリクエストが処理されること.
     * <p>
     * URLの予約文字についてはRFC3986 Appendix Aを参照のこと。
     * </p>
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・各リクエストパラメータに指定した文字列がそのまま返されること
     * </pre>
     * 
     * @throws UnsupportedEncodingException URLエンコーディングに失敗した場合
     */
    @Test
    public void testRequestParametersWithURLEncodedReservedCharacters() throws UnsupportedEncodingException {
        final String value = ":/?#[]@!$'()+,;";
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/unique/test/ping");
        builder.append("?key1=" + URLEncoder.encode(value, "UTF-8"));
        builder.append("&key2=" + URLEncoder.encode(value, "UTF-8"));
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        builder.append("&key3=" + URLEncoder.encode(value, "UTF-8"));
        builder.append("&key4=" + URLEncoder.encode(value, "UTF-8"));

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString("path"), is("GET /unique/test/ping"));
        assertThat(response.getString("key1"), is(value));
        assertThat(response.getString("key2"), is(value));
        assertThat(response.getString("key3"), is(value));
        assertThat(response.getString("key4"), is(value));
//
//        try {
//            HttpUriRequest request = new HttpGet(builder.toString());
//            JSONObject root = sendRequest(request);
//            assertResultOK(root);
//            assertEquals("GET /unique/test/ping", root.getString("path"));
//            assertEquals(value, root.getString("key1"));
//            assertEquals(value, root.getString("key2"));
//            assertEquals(value, root.getString("key3"));
//            assertEquals(value, root.getString("key4"));
//        } catch (JSONException e) {
//            fail("Exception in JSONObject." + e.getMessage());
//        }
    }

    /**
     * POSTリクエストのボディ部とパラメータ部の両方にリクエストパラメータを指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・キーが同じパラメータについては、ボディでの指定が優先されること。
     * </pre>
     * 
     * @throws UnsupportedEncodingException リクエストのBodyのエンコーディングに失敗した場合
     */
    @Test
    public void testPostRequestParametersBothBodyAndParameterPart() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/unique/test/ping");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=unknown");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        Map<String, Object> data = new HashMap<>();
        data.put(DConnectProfileConstants.PARAM_SERVICE_ID, URLEncoder.encode(getServiceId(), "UTF-8"));
        data.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, URLEncoder.encode(getAccessToken(), "UTF-8"));

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), data);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

//        try {
//            HttpPost request = new HttpPost(builder.toString());
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId()));
//            params.add(new BasicNameValuePair(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken()));
//            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            JSONObject root = sendRequest(request);
//            assertResultOK(root);
//        } catch (JSONException e) {
//            fail("Exception in JSONObject." + e.getMessage());
//        }
    }

    /**
     * PUTリクエストのボディ部とパラメータ部の両方にリクエストパラメータを指定するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /unique/test/ping?serviceId=unknown&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・キーが同じパラメータについては、ボディでの指定が優先されること。
     * </pre>
     * 
     * @throws UnsupportedEncodingException リクエストのBodyのエンコーディングに失敗した場合
     */
    @Test
    public void testPutRequestParametersBothBodyAndParameterPart() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/unique/test/ping");
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=unknown");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        Map<String, Object> data = new HashMap<>();
        data.put(DConnectProfileConstants.PARAM_SERVICE_ID, URLEncoder.encode(getServiceId(), "UTF-8"));
        data.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, URLEncoder.encode(getAccessToken(), "UTF-8"));

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), data);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

//        try {
//            HttpPut request = new HttpPut(builder.toString());
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId()));
//            params.add(new BasicNameValuePair(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken()));
//            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            JSONObject root = sendRequest(request);
//            assertResultOK(root);
//        } catch (JSONException e) {
//            fail("Exception in JSONObject." + e.getMessage());
//        }
    }
}
