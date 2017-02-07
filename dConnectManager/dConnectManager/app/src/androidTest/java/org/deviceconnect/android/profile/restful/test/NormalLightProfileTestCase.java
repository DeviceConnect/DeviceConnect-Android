/*
 NormalLightProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.test.plugin.profile.TestLightProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Lightプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalLightProfileTestCase extends RESTfulDConnectTestCase implements 
        TestLightProfileConstants {

    /**
     * ライト情報要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /light?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetLight() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ライト点灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /light?serviceId=xxxx&lightId=xxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLight() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ライト消灯要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light?serviceId=xxxx&lightId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLight() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ライト情報更新要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /light?serviceId=xxxx&lightId=xxx&name=xxxx&color=ff0000&brightness=0.5&flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(LightProfile.PROFILE_NAME);
        builder.addParameter(AuthorizationProfile.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        builder.addParameter(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        builder.addParameter(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        builder.addParameter(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        builder.addParameter(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 数値から文字列の色情報を作成する.
     * @param color 色情報
     * @return 文字列(RRGGBB)
     */
    private String convertColor(final Integer color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format("%02x%02x%02x", r, g, b);
    }

    /**
     * 点滅情報を配列から文字列を作成する.
     * @param flashing 点滅情報
     * @return 点滅情報の文字列
     */
    private String convertFlashing(final long[] flashing) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < flashing.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(flashing[i]);
        }
        return sb.toString();
    }
}
