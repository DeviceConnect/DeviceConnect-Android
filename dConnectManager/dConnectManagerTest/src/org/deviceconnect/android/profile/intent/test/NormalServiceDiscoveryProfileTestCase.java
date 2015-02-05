/*
 NormalServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import org.deviceconnect.android.test.plugin.profile.TestServiceDiscoveryProfileConstants;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;


/**
 * Service Discoveryプロファイルの正常系テスト.
 * 
 * @author NTT DOCOMO, INC.
 */
public class NormalServiceDiscoveryProfileTestCase extends IntentDConnectTestCase {
    /**
     * コンストラクタ.
     * 
     * @param string テストタグ
     */
    public NormalServiceDiscoveryProfileTestCase(final String string) {
        super(string);
    }

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
    public void testGetServices() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(IntentDConnectMessage.EXTRA_PROFILE, ServiceDiscoveryProfileConstants.PROFILE_NAME);
        Intent response = sendRequest(request);

        assertResultOK(response);
        Parcelable[] services =
                (Parcelable[]) response.getParcelableArrayExtra(ServiceDiscoveryProfileConstants.PARAM_SERVICES);

        assertTrue("services not found.", services.length > 0);
        boolean isFoundName = false;
        for (int i = 0; i < services.length; i++) {
            Bundle service = (Bundle) services[i];
            String name = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
            String id = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
            String type = service.getString(ServiceDiscoveryProfileConstants.PARAM_TYPE);
            assertNotNull("service.name is null", name);
            assertNotNull("service.id is null", id);
            assertNotNull("service.type is null", type);
            if (name.equals(TestServiceDiscoveryProfileConstants.DEVICE_NAME)) {
                isFoundName = true;
                break;
            }
        }
        if (!isFoundName) {
            fail("Not found Test DevicePlugin.");
        }
    }
}
