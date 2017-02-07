/*
 NormalMediaPlayerProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestMediaPlayerProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * MediaPlayerプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalMediaPlayerProfileTestCase extends RESTfulDConnectTestCase
    implements TestMediaPlayerProfileConstants {

    /**
     * 再生コンテンツの変更要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/media?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutMedia() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MEDIA);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(MediaPlayerProfileConstants.PARAM_MEDIA_ID, TEST_MEDIA_ID);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生コンテンツ情報の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/media?serviceId=xxxx&mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMedia() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MEDIA);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(MediaPlayerProfileConstants.PARAM_MEDIA_ID, TEST_MEDIA_ID);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生コンテンツ一覧の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/media_list?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMediaList() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MEDIA_LIST);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * コンテンツ再生状態の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/play_status?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetPlayStatus() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_PLAY_STATUS);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メディアプレイヤーの再生要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/play?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutPlay() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_PLAY);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メディアプレイヤーの停止要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/stop?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutStop() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_STOP);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メディアプレイヤーの一時停止要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/pause?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutPause() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_PAUSE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メディアプレイヤーの一時停止解除要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/resume?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutResume() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_RESUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生位置の変更要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/seek?serviceId=xxxx&pos=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSeek() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_SEEK);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(MediaPlayerProfileConstants.PARAM_POS, String.valueOf(0));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生位置の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/seek?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSeek() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_SEEK);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生音量の変更要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/volume?serviceId=xxxx&volume=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutVolume() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(MediaPlayerProfileConstants.PARAM_VOLUME, String.valueOf(TEST_VOLUME));

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生音量の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/volume?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetVolume() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_VOLUME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ミュートを有効にする要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/mute?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutMute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MUTE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ミュートを無効にする要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediaPlayer/mute?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteMute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MUTE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * ミュート状態の取得要求を送信するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaPlayer/mute?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetMute() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(MediaPlayerProfileConstants.PROFILE_NAME);
        builder.setAttribute(MediaPlayerProfileConstants.ATTRIBUTE_MUTE);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * コンテンツ再生状態変化通知のコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaPlayer/onStatusChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOnStatusChangePlay() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaPlayerProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaPlayerProfileConstants.ATTRIBUTE_ON_STATUS_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 再生コンテンツ再生状態変化通知のコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediaPlayer/onStatusChange?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeleteOnPlayStatusChange() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaPlayerProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaPlayerProfileConstants.ATTRIBUTE_ON_STATUS_CHANGE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
