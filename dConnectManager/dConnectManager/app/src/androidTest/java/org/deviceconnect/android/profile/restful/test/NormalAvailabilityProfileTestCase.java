/*
 NormalAvailabilityProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.http.HttpUtil;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Availabilityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalAvailabilityProfileTestCase extends RESTfulDConnectTestCase {

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    /**
     * サーバ起動確認テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /availability
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetAvailability() throws Exception {
        String uri = "http://localhost:4035/gotapi/availability";

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", getContext().getPackageName());

        HttpUtil.Response response = HttpUtil.get(uri, headers);
        assertThat(response, is(notNullValue()));

        JSONObject json = response.getJSONObject();
        assertThat(json, is(notNullValue()));
        assertThat(json.getInt("result"), is(0));
    }
}
