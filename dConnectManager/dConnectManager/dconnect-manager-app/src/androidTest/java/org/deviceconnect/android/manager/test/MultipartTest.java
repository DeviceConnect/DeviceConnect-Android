/*
 MultipartTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.entity.BinaryEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * リクエストのマルチパート解析処理のテスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class MultipartTest extends RESTfulDConnectTestCase {
    /**
     * POSTリクエストパラメータをマルチパートで指定可能であることのテスト.
     * <pre>
     * Method: POST
     * Path: /notification/notify
     * Body: serviceId=xxxx&amp;type=0&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile("dataTest");

        MultipartEntity body = new MultipartEntity();
        body.add("serviceId", new StringEntity(getServiceId()));
        body.add("accessToken", new StringEntity(getAccessToken()));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * PUTリクエストパラメータをマルチパートで指定可能であることのテスト.
     * <pre>
     * Method: PUT
     * Path: /deviceOrientation/onDeviceOrientation
     * Body: serviceId=xxxx&amp;sessionKey=xxxx&amp;accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testParsingMultipartAsRequestParametersMethodPut() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile("dataTest");

        MultipartEntity body = new MultipartEntity();
        body.add("serviceId", new StringEntity(getServiceId()));
        body.add("accessToken", new StringEntity(getAccessToken()));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 0バイトのファイルも送信可能であることのテスト.
     * <pre>
     * Method: POST
     * Path: /file/send?serviceId=xxxx&amp;filename=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testSendZeroByteFile() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile("dataTest");
        builder.addParameter("serviceId", getServiceId());
        builder.addParameter("accessToken", getAccessToken());

        MultipartEntity body = new MultipartEntity();
        body.add("data", new BinaryEntity(new byte[0]));

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), body);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getInt("fileSize"), is(0));
    }
}
