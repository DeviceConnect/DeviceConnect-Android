/*
 NormalDeviceOrientationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * DeviceOrientationプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalDeviceOrientationProfileTestCase extends IntentDConnectTestCase {

    /**
     * ondeviceorientationイベントのコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=deviceorientation
     *     attribute=ondeviceorientation
     *     sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testPutOnDeviceOrientation() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, DeviceOrientationProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE,
                DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ondeviceorientationイベントのコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=deviceorientation
     *     attribute=ondeviceorientation
     *     sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnDeviceOrientation() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, DeviceOrientationProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE,
                DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

}
