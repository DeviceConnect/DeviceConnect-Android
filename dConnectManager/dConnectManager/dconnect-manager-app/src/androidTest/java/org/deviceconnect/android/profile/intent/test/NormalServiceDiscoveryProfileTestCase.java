/*
 NormalServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Service Discoveryプロファイルの正常系テスト.
 * 
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalServiceDiscoveryProfileTestCase extends IntentDConnectTestCase {
    /**
     * サービスの探索を行う.
     * 
     * <pre>
     * 【Intent通信】
     * Method: GET
     * Extra:
     *     profile=serviceDiscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・servicesに少なくとも1つ以上のサービスが発見されること。
     * ・servicesの中に「Test Success Device」のnameを持ったサービスが存在すること。
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
        assertThat(services, is(CoreMatchers.notNullValue()));
        assertThat(services.size(), is(greaterThan(0)));
        for (Object obj : services) {
            DConnectMessage service = (DConnectMessage) obj;
            String id = service.getString("id");
            String name = service.getString("name");
            assertThat(id, is(CoreMatchers.notNullValue()));
            assertThat(name, is(CoreMatchers.notNullValue()));
        }
    }
}
