/*
 NormalBatteryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Batteryプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalBatteryProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * バッテリー全属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBattery() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーlevel属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/level?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryLevel() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_LEVEL);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーcharging属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/charging?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryCharging() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_CHARGING);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーchargingTime属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/chargingTime?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryChargingTime() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_CHARGING_TIME);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーdischargingTime属性取得テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /battery/dischargingTime?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetBatteryDischargingTime() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_DISCHARGING_TIME);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーonChargingChangeを登録するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /battery/onChargingChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBatteryOnChargingChange() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * バッテリーonChargingChangeを解除するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /battery/onChargingChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBatteryOnChargingChange() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_CHARGING_CHANGE);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * onBatteryChange属性のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /battery/onBatteryChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutBatteryOnBatteryChange() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * onBatteryChange属性のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /battery/onBatteryChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteBatteryOnBatteryChange() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(BatteryProfileConstants.PROFILE_NAME);
        builder.setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE);
        builder.setServiceId(getServiceId());
        builder.setAccessToken(getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
