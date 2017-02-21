/*
 NormalServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


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
     *     profile=servicediscovery
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
//        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
//        request.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID, getServiceId());
//        request.putExtra(IntentDConnectMessage.EXTRA_PROFILE, ServiceDiscoveryProfileConstants.PROFILE_NAME);
//        Intent response = sendRequest(request);
//        assertResultOK(response);
    }
}
