/*
 NormalFilesProfileTestCase.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Filesプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalFilesProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * テスト用デバイスプラグインのファイルへのURIを定義.
     */
    private static final String TEST_URI = "content://org.deviceconnect.android.deviceplugin.test.provider/test.dat";

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    /**
     * デバイス一覧取得リクエストを送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /files?uri=TEST_URI
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTPステータスコードに200が返ってくること。
     * ・プラグインで指定したデータサイズが取得できること。
     * </pre>
     */
    @Test
    public void testFiles() throws Exception {
        String uri = "http://localhost:4035/gotapi/files";
        uri += "?uri=" + URLEncoder.encode(TEST_URI, "UTF-8");

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getOrigin());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(200));
        assertThat((int) response.getBody().length(), is(1024 * 1024));
    }
}
