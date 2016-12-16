/*
 FailHTTPServerTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.android.test.http.HttpUtil;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
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
    private static final int VERY_LONG_SERVICE_ID_LENGTH = 10000;

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
        byte[] buf = HttpUtil.connect("HEAD", DCONNECT_MANAGER_URI, null, null);
        assertThat(buf, is(nullValue()));
    }

    /**
     * HTTPヘッダのサイズが8KBを超えるHTTPリクエストを送信する異常系テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId=xxxx&accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・HTTP 413 Request Entity Too Largeが返ること。
     * </pre>
     */
    @Test
    public void testHTTPHeaderOver8KB() throws IOException {
        // HTTPヘッダのサイズを8KBにするために、10000文字のサービスIDを設定する
        StringBuilder serviceId = new StringBuilder();
        for (int i = 0; i < VERY_LONG_SERVICE_ID_LENGTH; i++) {
            serviceId.append("0");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/battery");
        builder.append("?");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + serviceId.toString());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
    }
}
