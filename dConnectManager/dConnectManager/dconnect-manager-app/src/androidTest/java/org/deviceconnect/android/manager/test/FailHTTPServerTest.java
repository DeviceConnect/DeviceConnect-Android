/*
 FailHTTPServerTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.android.test.http.HttpUtil;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * HTTPサーバの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailHTTPServerTest extends RESTfulDConnectTestCase {

    /**
     * {@link #testHTTPHeaderOver8KB()}のサービスID.
     */
    private static final int VERY_LONG_SERVICE_ID_LENGTH = 8 * 1024;

    /**
     * HEADメソッドでHTTPサーバにアクセスする異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: HEAD
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTP 501 Not Implementedが返ること。
     * </pre>
     */
    @Test
    public void testHttpMethodHead() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());
        HttpUtil.Response response = HttpUtil.connect("HEAD", MANAGER_URI, headers, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(501));
    }

    /**
     * HTTPヘッダのサイズが8KBを超えるHTTPリクエストを送信する異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId=xxxx&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTP 413 Request Entity Too Largeが返ること。
     * </pre>
     */
    @Test
    public void testHTTPHeaderOver8KB() throws Exception {
        // HTTPヘッダのサイズを8KBにするために、8192文字のサービスIDを設定する
        StringBuilder serviceId = new StringBuilder();
        for (int i = 0; i < VERY_LONG_SERVICE_ID_LENGTH; i++) {
            serviceId.append("0");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/battery?accessToken=");
        builder.append(getAccessToken());

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());
        headers.put(DConnectProfileConstants.PARAM_SERVICE_ID, serviceId.toString());

        HttpUtil.Response response = HttpUtil.get(builder.toString(), headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(413));
    }

    /**
     * API無しでHTTPサーバにアクセスする異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTP 400 Bad Requestが返ること。
     * ・resultに1が返ること。
     * </pre>
     */
    @Test
    public void testEmptyAPI() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get("http://localhost:4035/", headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(400));

        JSONObject json = response.getJSONObject();
        assertThat(json.getInt("result"), is(1));
        assertThat(json.getInt("errorCode"), is(19));
        assertThat(json.getString("errorMessage"), is(notNullValue()));
    }

    /**
     * Profile無しでHTTPサーバにアクセスする異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /gotapi
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTP 400 Bad Requestが返ること。
     * ・resultに1が返ること。
     * </pre>
     */
    @Test
    public void testEmptyProfile() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(MANAGER_URI, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(400));

        JSONObject json = response.getJSONObject();
        assertThat(json.getInt("result"), is(1));
        assertThat(json.getInt("errorCode"), is(19));
        assertThat(json.getString("errorMessage"), is(notNullValue()));
    }

    /**
     * Origin無しでHTTPサーバにアクセスする異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /gotapi/serviceDiscovery
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ること。
     * </pre>
     */
    @Test
    public void testEmptyOrigin() throws Exception {
        HttpUtil.Response response = HttpUtil.get(MANAGER_URI + "/serviceDiscovery");
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(200));

        JSONObject json = response.getJSONObject();
        assertThat(json.getInt("result"), is(1));
    }
}
