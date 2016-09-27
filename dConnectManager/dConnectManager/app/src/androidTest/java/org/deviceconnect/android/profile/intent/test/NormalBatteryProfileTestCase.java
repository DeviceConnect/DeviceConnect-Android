/*
 NormalBatteryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Batteryプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalBatteryProfileTestCase extends IntentDConnectTestCase {
    /**
     * バッテリー全属性取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Profile: battery
     * Interface: なし
     * Attribute: なし
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingがfalseで返ってくること。
     * ・chargingtimeが50000.0で返ってくること。
     * ・dischargingtimeが10000.0で返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testBattery() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        String serviceId = getServiceId();
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * charging属性取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Profile: battery
     * Interface: なし
     * Attribute: charging
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingがfalseで返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryCharging() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_CHARGING);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * chargingTime属性取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Profile: battery
     * Interface: なし
     * Attribute: なし
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・chargingTimeが50000.0で返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryChargingTime() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_CHARGING_TIME);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * dischargingTime属性取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Profile: battery
     * Interface: なし
     * Attribute: dischargingTime
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・dischargingTimeが10000.0で返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryDischargingTime() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_DISCHARGING_TIME);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * level属性取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Profile: battery
     * Interface: なし
     * Attribute: なし
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryLevel() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_LEVEL);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * onchargingchange属性のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Profile: battery
     * Interface: なし
     * Attribute: onchargingchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testBatteryOnChargingChange01() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * onchargingchange属性のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Profile: battery
     * Interface: なし
     * Attribute: onchargingchange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryOnChargingChange02() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * onbatterychange属性のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Profile: battery
     * Interface: なし
     * Attribute: onbatterychange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testBatteryOnBatteryChange01() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * onbatterychange属性のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Profile: battery
     * Interface: なし
     * Attribute: onbatterychange
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testBatteryOnBatteryChange02() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

}
