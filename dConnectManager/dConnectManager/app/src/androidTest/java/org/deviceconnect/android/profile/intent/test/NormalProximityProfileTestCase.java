/*
 NormalProximityProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;




/**
 * Proximityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalProximityProfileTestCase extends IntentDConnectTestCase {

    /**
     * 近接センサーによる物の検知のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=proximity
     *     attribute=ondeviceproximity
     *     session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnDeviceProximity01() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, ProximityProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);

        Intent response = sendRequest(request);

        assertResultOK(response);

        Intent event = waitForEvent();
        assertNotNull(event);
    }

    /**
     * 近接センサーによる物の検知のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=proximity
     *     attribute=ondeviceproximity
     *     session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnDeviceProximity02() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, ProximityProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);

        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * 近接センサーによる人の検知のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=proximity
     *     attribute=onuserproximity
     *     session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnUserProximity01() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, ProximityProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ProximityProfileConstants.ATTRIBUTE_ON_USER_PROXIMITY);

        Intent response = sendRequest(request);
        assertResultOK(response);

        Intent event = waitForEvent();
        assertNotNull(event);
    }

    /**
     * 近接センサーによる人の検知のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=proximity
     *     attribute=onuserproximity
     *     session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnUserProximity02() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, ProximityProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ProximityProfileConstants.ATTRIBUTE_ON_USER_PROXIMITY);

        Intent response = sendRequest(request);

        assertResultOK(response);
    }

}
