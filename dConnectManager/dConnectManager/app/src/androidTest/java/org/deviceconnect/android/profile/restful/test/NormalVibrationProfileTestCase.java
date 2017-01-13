/*
 NormalVibrationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Vibratorプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalVibrationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * Vibration開始要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /vibration/vibrate?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVibrate001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + VibrationProfileConstants.PROFILE_NAME);
        builder.append("/" + VibrationProfileConstants.ATTRIBUTE_VIBRATE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&" + AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        // パターンのデータを追加
        MultipartEntity body = new MultipartEntity();
        body.add(VibrationProfileConstants.PARAM_PATTERN, new StringEntity("100,100,100,100"));

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Vibration開始要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /vibration/vibrate?serviceId=xxxx&pattern=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVibrate002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + VibrationProfileConstants.PROFILE_NAME);
        builder.append("/" + VibrationProfileConstants.ATTRIBUTE_VIBRATE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(VibrationProfileConstants.PARAM_PATTERN + "=500,500,500");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * Vibration停止要求を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /vibration/vibrate?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteVibrate() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + VibrationProfileConstants.PROFILE_NAME);
        builder.append("/" + VibrationProfileConstants.ATTRIBUTE_VIBRATE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
