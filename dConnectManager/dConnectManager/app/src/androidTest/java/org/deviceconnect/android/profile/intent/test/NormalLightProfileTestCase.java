/*
 NormalLightProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.test.plugin.profile.TestLightProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Lightプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalLightProfileTestCase extends IntentDConnectTestCase implements TestLightProfileConstants {
    /**
     * ライト情報要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=light
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・lightsにライト情報が格納されていること。
     * ・lightIdにライトID(test_light_id)が格納されていること。
     * ・nameにライト名(test_light_name)が格納されていること。
     * ・onにライトの状態が格納されていること。
     * </pre>
     */
    @Test
    public void testGetLight() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        Intent response = sendRequest(request);
        assertResultOK(response);
        Parcelable[] lights = response.getParcelableArrayExtra(LightProfile.PARAM_LIGHTS);
        assertEquals("lights length is not equals.", lights.length, 1);
        Bundle light = (Bundle) lights[0];
        assertEquals("lightId is not equals.", LIGHT_ID, light.getString(LightProfile.PARAM_LIGHT_ID));
        assertEquals("name is not equals.", LIGHT_NAME, light.getString(LightProfile.PARAM_NAME));
        assertEquals("on is not equals.", LIGHT_ON, light.getBoolean(LightProfile.PARAM_ON));
        assertEquals("config is not equals.", LIGHT_CONFIG, light.getString(LightProfile.PARAM_CONFIG));
    }

    /**
     * ライト点灯要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: POST
     * Extra: 
     *     profile=light
     *     serviceId=xxxx
     *     lightId=xxxx
     *     color=ff0000
     *     brightness=0.5
     *     flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLight() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_POST);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        request.putExtra(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        request.putExtra(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        request.putExtra(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ライト消灯要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=light
     *     serviceId=xxxx
     *     lightId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLight() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }


    /**
     * ライト情報更新要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: POST
     * Extra: 
     *     profile=light
     *     serviceId=xxxx
     *     lightId=xxxx
     *     name=xxxx
     *     color=ff0000
     *     brightness=0.5
     *     flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_POST);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_LIGHT_ID, LIGHT_ID);
        request.putExtra(LightProfile.PARAM_NAME, LIGHT_NEW_NAME);
        request.putExtra(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        request.putExtra(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        request.putExtra(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ライトグループ情報要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=light
     *     attribute=group
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・lightGroupsにライトグループ情報が格納されていること。
     * ・lightsにライト情報が格納されていること。
     * ・lightIdにライトID(test_light_id)が格納されていること。
     * ・nameにライト名(test_light_name)が格納されていること。
     * ・onにライトの状態が格納されていること。
     * </pre>
     */
    @Test
    public void testGetLightGroup() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());

        Intent response = sendRequest(request);
        assertResultOK(response);
        Parcelable[] lightGroups = response.getParcelableArrayExtra(LightProfile.PARAM_LIGHT_GROUPS);
        assertEquals("lightGroups length is not equals.", lightGroups.length, 1);
        Bundle lightGroup = (Bundle) lightGroups[0];
        assertEquals("groupId is not equals.", LIGHT_GROUP_ID, lightGroup.getString(LightProfile.PARAM_GROUP_ID));
        assertEquals("groupName is not equals.", LIGHT_GROUP_NAME, lightGroup.getString(LightProfile.PARAM_NAME));
        Parcelable[] lights = lightGroup.getParcelableArray(LightProfile.PARAM_LIGHTS);
        Bundle light = (Bundle) lights[0];
        assertEquals("lightId is not equals.", LIGHT_ID, light.getString(LightProfile.PARAM_LIGHT_ID));
        assertEquals("name is not equals.", LIGHT_NAME, light.getString(LightProfile.PARAM_NAME));
        assertEquals("on is not equals.", LIGHT_ON, light.getBoolean(LightProfile.PARAM_ON));
        assertEquals("config is not equals.", LIGHT_CONFIG, light.getString(LightProfile.PARAM_CONFIG));
    }

    /**
     * ライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: POST
     * Extra: 
     *     profile=light
     *     serviceId=xxxx
     *     groupId=xxxx
     *     color=ff0000
     *     brightness=0.5
     *     flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPostLightGroup() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_POST);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        request.putExtra(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        request.putExtra(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        request.putExtra(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ライトグループ消灯要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=light
     *     attribute=group
     *     serviceId=xxxx
     *     groupId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroup() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ライトグループ点灯要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=light
     *     attribute=group
     *     serviceId=xxxx
     *     groupId=xxxx
     *     name=xxxx
     *     color=ff0000
     *     brightness=0.5
     *     flashing=1000,1001,1002
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLightGroup() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);
        request.putExtra(LightProfile.PARAM_NAME, LIGHT_NEW_GROUP_NAME);
        request.putExtra(LightProfile.PARAM_COLOR, convertColor(LIGHT_COLOR));
        request.putExtra(LightProfile.PARAM_BRIGHTNESS, String.valueOf(LIGHT_BRIGHTNESS));
        request.putExtra(LightProfile.PARAM_FLASHING, convertFlashing(LIGHT_FLASHING));

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ライトグループ作成要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: POST
     * Extra: 
     *     profile=light
     *     attribute=group
     *     serviceId=xxxx
     *     groupName=xxx
     *     lightIds=xxx,xxx,xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・groupIdが格納されていること。
     * </pre>
     */
    @Test
    public void testPostLightGroupCreate() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_POST);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_CREATE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_GROUP_NAME, LIGHT_NEW_GROUP_NAME);
        request.putExtra(LightProfile.PARAM_LIGHT_IDS, convertLightIds(LIGHT_IDS));

        Intent response = sendRequest(request);
        assertResultOK(response);
        assertEquals("groupId is not equals.", LIGHT_NEW_GROUP_ID, 
                response.getStringExtra(LightProfile.PARAM_GROUP_ID));
    }

    /**
     * ライトグループ削除要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=light
     *     attribute=group
     *     serviceId=xxxx
     *     groupId=xxx
     * 【HTTP通信】
     * Method: DELETE
     * Path: /light/group/clear?serviceId=xxxx&groupId=xxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteLightGroupClear() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, LightProfile.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, LightProfile.ATTRIBUTE_GROUP);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, LightProfile.ATTRIBUTE_CLEAR);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(LightProfile.PARAM_GROUP_ID, LIGHT_GROUP_ID);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 数値から文字列の色情報を作成する.
     * @param color 色情報
     * @return 文字列(RRGGBB)
     */
    private String convertColor(final Integer color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format("%02x%02x%02x", r, g, b);
    }

    /**
     * 点滅情報を配列から文字列を作成する.
     * @param flashing 点滅情報
     * @return 点滅情報の文字列
     */
    private String convertFlashing(final long[] flashing) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < flashing.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(flashing[i]);
        }
        return sb.toString();
    }

    /**
     * ライトIDリストから文字列を作成する.
     * @param lightIds ライトIDリスト
     * @return ライトIDリストの文字列
     */
    private String convertLightIds(final String[] lightIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lightIds.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(lightIds[i]);
        }
        return sb.toString();
    }
}
