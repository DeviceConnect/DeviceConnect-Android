/*
 NormalSettingProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestSettingProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.SettingProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * の正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalSettingProfileTestCase extends IntentDConnectTestCase
    implements TestSettingProfileConstants {

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.ALARM.getValue());
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=2
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.CALL.getValue());
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume003() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.RINGTONE.getValue());
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=4
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume004() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.MAIL.getValue());
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=5
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが0.5で返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume005() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.OTHER.getValue());
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=1
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.ALARM.getValue());
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=2
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.CALL.getValue());
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=3
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume003() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.RINGTONE.getValue());
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=4
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume004() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.MAIL.getValue());
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの音量設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=volume
     *     kind=5
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume005() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_SOUND);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(SettingProfileConstants.PARAM_KIND, SettingProfileConstants.VolumeKind.OTHER.getValue());
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの日時取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=date
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・dateが"2014-01-01T01:01:01+09:00"で返ってくること。
     * </pre>
     */
    @Test
    public void testGetDate() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_DATE);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスの日時設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=date
     *     date=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutDate() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_DATE);
        request.putExtra(SettingProfileConstants.PARAM_DATE, TestSettingProfileConstants.DATE);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=1
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
     * </pre>
     */
    @Test
    public void testGetLight001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 1);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=2
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
     * </pre>
     */
    @Test
    public void testGetLight002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 2);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・levelが50で返ってくること。
     * </pre>
     */
    @Test
    public void testGetLight003() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 3);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=1
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 1);
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=1
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 2);
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * スマートデバイスのライト明度設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     attribute=light
     *     kind=1
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutLight003() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_BRIGHTNESS);
        request.putExtra(SettingProfileConstants.PARAM_KIND, 3);
        request.putExtra(SettingProfileConstants.PARAM_LEVEL, TestSettingProfileConstants.LEVEL);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 画面消灯設定の取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=settings
     *     interface=display
     *     attribute=sleep
     *     kind=3
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・timeが0で返ってくること。
     * </pre>
     */
    @Test
    public void testGetSleep() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_SLEEP);
        Intent response = sendRequest(request);

        assertResultOK(response);
    }

    /**
     * 画面消灯設定の設定テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     profile=settings
     *     interface=display
     *     attribute=sleep
     *     kind=1
     *     level=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSleep() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SettingProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SettingProfileConstants.INTERFACE_DISPLAY);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SettingProfileConstants.ATTRIBUTE_SLEEP);
        request.putExtra(SettingProfileConstants.PARAM_TIME, 1);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }
}
