/*
 FailFilesProfileTestCase.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Filesプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailFilesProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * テスト用デバイスプラグインのファイルへのURIを定義.
     */
    private static final String TEST_URI = "content://org.deviceconnect.android.deviceplugin.test.provider/test.dat";

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    /**
     * filesプロファイルにPUTメソッドでアクセスする.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /gotpai/files?uri=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに400が返却されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPutFiles() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";
        uri += "?uri=" + URLEncoder.encode(TEST_URI, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.put(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(400));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(1));
    }

    /**
     * filesプロファイルにPOSTメソッドでアクセスする.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /gotpai/files?uri=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに400が返却されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testPostFiles() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";
        uri += "?uri=" + URLEncoder.encode(TEST_URI, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.post(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(400));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(1));
    }

    /**
     * filesプロファイルにDELETETメソッドでアクセスする.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /gotpai/files?uri=xxxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに400が返却されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteFiles() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";
        uri += "?uri=" + URLEncoder.encode(TEST_URI, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.delete(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(400));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(1));
    }

    /**
     * filesプロファイルに存在しないURIに対してアクセスする。
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /gotpai/files?uri=存在しないURI
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに404が返却されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testFilesNotFoundUri() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";
        uri += "?uri=abc";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(404));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(1));
    }

    /**
     * filesプロファイルにURI無しでアクセスする。
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /gotpai/files
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに404が返却されること。
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testFilesWithoutUri() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(404));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(1));
    }
}
