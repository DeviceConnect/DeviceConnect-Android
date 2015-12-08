/*
 NormalServiceInformationProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Service Informationプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalServiceInformationProfileTestCase extends IntentDConnectTestCase {

    /**
     * デバイスのシステムプロファイルを取得する.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=serviceinformation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystemDevice() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, ServiceInformationProfileConstants.PROFILE_NAME);
        Intent response = sendRequest(request);

        assertResultOK(response);
        String[] supports = response.getStringArrayExtra(ServiceInformationProfileConstants.PARAM_SUPPORTS);
        assertNotNull(supports);
        Bundle connect = response.getBundleExtra(ServiceInformationProfileConstants.PARAM_CONNECT);
        assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_WIFI));
        assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_BLUETOOTH));
        assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_NFC));
        assertEquals(false, connect.getBoolean(ServiceInformationProfileConstants.PARAM_BLE));
    }

}
