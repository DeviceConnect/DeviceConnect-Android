/*
 NormalServiceInformationProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestSystemProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Service Informationプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalServiceInformationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * デバイスのシステムプロファイルを取得する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceinformation?deviceid=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・versionにStringが返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDevice() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + ServiceInformationProfileConstants.PROFILE_NAME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject resp = sendRequest(request);
            assertNotNull("root is null.", resp);
            assertEquals(DConnectMessage.RESULT_OK,
                    resp.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(TestSystemProfileConstants.VERSION, 
                    resp.getString(ServiceInformationProfileConstants.PARAM_VERSION));
            JSONObject connect = resp.getJSONObject(ServiceInformationProfileConstants.PARAM_CONNECT);
            assertNotNull(connect);
            assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_WIFI));
            assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_BLUETOOTH));
            assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_NFC));
            assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_BLE));
            JSONArray supports = resp.getJSONArray(ServiceInformationProfileConstants.PARAM_SUPPORTS);
            assertNotNull(supports);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

}
