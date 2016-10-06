/*
 NormalSystemProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestSystemProfileConstants;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Systemプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalSystemProfileTestCase extends RESTfulDConnectTestCase
    implements TestSystemProfileConstants {

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * デバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /system
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・versionにString型の値が返ってくること。
     * ・supportsにJSONArray型の値が返ってくること。
     * ・pluginsにテスト用デバイスプラグインの情報が含まれていること。
     * </pre>
     */
    @Test
    public void testGetSystem() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + SystemProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したセッションキーに対応するイベントを全て解除する.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /system/events?sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     * @throws JSONException JSON解析に失敗した場合
     */
    @Test
    public void testDeleteSystemEvents() throws JSONException {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_EVENTS);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
}
