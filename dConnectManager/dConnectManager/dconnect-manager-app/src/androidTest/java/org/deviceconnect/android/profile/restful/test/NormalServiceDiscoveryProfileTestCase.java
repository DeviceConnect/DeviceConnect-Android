/*
 NormalServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Network Service Discoveryプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalServiceDiscoveryProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * デバイス一覧取得リクエストを送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceDiscovery
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・servicesに少なくとも1つ以上のサービスが発見されること。
     * </pre>
     */
    @Test
    public void testGetServices() {
        String uri = "http://localhost:4035/gotapi/serviceDiscovery";
        uri += "?accessToken=" + getAccessToken();

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

        List<Object> services = response.getList("services");
        assertThat(services, is(notNullValue()));
        assertThat(services.size(), is(greaterThan(0)));
        for (Object obj : services) {
            DConnectMessage service = (DConnectMessage) obj;
            String id = service.getString("id");
            String name = service.getString("name");
            assertThat(id, is(notNullValue()));
            assertThat(name, is(notNullValue()));
        }
    }
}
