/*
 NormalServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
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

    @Override
    protected boolean isSearchServices() {
        return false;
    }

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
     * ・servicesの中に「Test Success Device」のnameを持ったサービスが存在すること。
     * </pre>
     */
    @Test
    public void testGetServices() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

        List<Object> list = response.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
        assertThat(list, is(notNullValue()));
        assertThat(list.size(), is(greaterThan(0)));
        boolean result = false;
        for (Object obj : list) {
            DConnectMessage service = (DConnectMessage) obj;
            String name = service.getString(ServiceDiscoveryProfile.PARAM_NAME);
            if ("Test Success Device".equals(name)) {
                result = true;
            }
        }
        assertThat(result, is(true));
    }
}
