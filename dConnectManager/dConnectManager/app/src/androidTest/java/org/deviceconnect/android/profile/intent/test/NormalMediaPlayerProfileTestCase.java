/*
 NormalMediaPlayerProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestMediaPlayerProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * MediaPlayerプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalMediaPlayerProfileTestCase extends IntentDConnectTestCase
    implements TestMediaPlayerProfileConstants {

    /**
     * 再生コンテンツの変更要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=media
     *     serviceId=xxxx
     *     mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutMedia() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MEDIA);
        request.putExtra(MediaPlayerProfileConstants.PARAM_MEDIA_ID, TEST_MEDIA_ID);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生コンテンツ情報の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=media
     *     serviceId=xxxx
     *     mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMedia() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MEDIA);
        request.putExtra(MediaPlayerProfileConstants.PARAM_MEDIA_ID, TEST_MEDIA_ID);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生コンテンツ一覧の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=media_list
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMediaList() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MEDIA_LIST);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * コンテンツ再生状態の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=play_status
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetPlayStatus() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_PLAY_STATUS);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * メディアプレイヤーの再生要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=play
     *     serviceId=xxxx
     *     pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutPlay() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_PLAY);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * メディアプレイヤーの停止要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=stop
     *     serviceId=xxxx
     *     pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutStop() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_STOP);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * メディアプレイヤーの一時停止要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=pause
     *     serviceId=xxxx
     *     pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutPause() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_PAUSE);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * メディアプレイヤーの一時停止解除要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=resume
     *     serviceId=xxxx
     *     pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutResume() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_RESUME);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生位置の変更要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=seek
     *     serviceId=xxxx
     *     pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSeek() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_SEEK);
        request.putExtra(MediaPlayerProfileConstants.PARAM_POS, String.valueOf(0));

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生位置の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=seek
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSeek() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_SEEK);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生音量の変更要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=volume
     *     serviceId=xxxx
     *     volume=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_VOLUME);
        request.putExtra(MediaPlayerProfileConstants.PARAM_VOLUME, TEST_VOLUME);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 再生音量の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=volume
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_VOLUME);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ミュートを有効にする要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=mute
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutMute() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MUTE);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ミュートを無効にする要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=media_player
     *     attribute=mute
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteMute() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MUTE);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ミュート状態の取得要求を送信するテスト.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=media_player
     *     attribute=mute
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMute() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_MUTE);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * コンテンツ再生状態変化通知のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=media_player
     *     attribute=onstatuschange
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnStatusChangePlay() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, getClientId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_ON_STATUS_CHANGE);

        Intent response = sendRequest(request);
        assertResultOK(response);
        Intent event = waitForEvent();
        assertNotNull(event);
    }

    /**
     * 再生コンテンツ再生状態変化通知のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra: 
     *     profile=media_player
     *     attribute=onstatuschange
     *     serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeletePlayStatusChange() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, getClientId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, MediaPlayerProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaPlayerProfileConstants.ATTRIBUTE_ON_STATUS_CHANGE);

        Intent response = sendRequest(request);
        assertResultOK(response);
    }

}
