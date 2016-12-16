/*
 NormalMediaStreamRecordingProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.test.plugin.profile.TestMediaStreamRecordingProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * MediaStreamRecordingプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalMediaStreamRecordingProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * 指定したスマートデバイス上で使用可能なカメラ情報を取得するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaStreamRecording/mediaRecorder?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・recordersに長さ1のBundle配列が格納されていること。
     * ・recorder[0].idが"test_camera_0"であること。
     * ・recorder[0].stateが"inactive"であること。
     * ・recorder[0].imageWidthが1920であること。
     * ・recorder[0].imageHeightが1080であること。
     * ・recorder[0].previewWidthが640であること。
     * ・recorder[0].previewHeightが480であること。
     * ・recorder[0].previewMaxFrameRateが30.0であること。
     * ・recorder[0].mimeTypeが"video/mp4"であること。
     * ・recorder[0].configが"test_config"であること。
     * </pre>
     */
    @Test
    public void testGetMediaRecorder() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_MEDIARECORDER);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

        List recorders = response.getList(MediaStreamRecordingProfile.PARAM_RECORDERS);
        assertThat(recorders, is(notNullValue()));
        assertThat(recorders.size(), is(1));

        DConnectMessage recorder =  (DConnectMessage) recorders.get(0);
        assertThat(recorder.getString(MediaStreamRecordingProfile.PARAM_ID), is("test_camera_0"));
        assertThat(recorder.getString(MediaStreamRecordingProfile.PARAM_STATE), is("inactive"));
        assertThat(recorder.getInt(MediaStreamRecordingProfile.PARAM_IMAGE_WIDTH), is(1920));
        assertThat(recorder.getInt(MediaStreamRecordingProfile.PARAM_IMAGE_HEIGHT), is(1080));
        assertThat(recorder.getInt(MediaStreamRecordingProfile.PARAM_PREVIEW_WIDTH), is(640));
        assertThat(recorder.getInt(MediaStreamRecordingProfile.PARAM_PREVIEW_HEIGHT), is(480));
        assertThat(recorder.getFloat(MediaStreamRecordingProfile.PARAM_PREVIEW_MAX_FRAME_RATE), is(30.0f));
        assertThat(recorder.getString(MediaStreamRecordingProfile.PARAM_MIME_TYPE), is("video/mp4"));
        assertThat(recorder.getString(MediaStreamRecordingProfile.PARAM_CONFIG), is("test_config"));
    }

    /**
     * 指定したスマートデバイスに対して写真撮影依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/takePhoto?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testTakePhoto001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_TAKE_PHOTO);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());


        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
    }

    /**
     * 指定したスマートデバイスに対して写真撮影依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/takePhoto?serviceId=xxxx&target=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testTakePhoto002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_TAKE_PHOTO);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TARGET + "="
                + TestMediaStreamRecordingProfileConstants.ID);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/record?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testRecord001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_RECORD);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
        // TODO URI比較
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/record?serviceId=xxxx&target=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testRecord002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_RECORD);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TARGET + "="
                + TestMediaStreamRecordingProfileConstants.ID);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
        // TODO URI比較
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/record?serviceId=xxxx&timeslice=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testRecord003() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_RECORD);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TIME_SLICE + "=3600");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
        // TODO URI比較
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediaStreamRecording/record?serviceId=xxxx&target=xxxx&timeslice=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・uriが"test.mp4"であること。
     * </pre>
     */
    @Test
    public void testRecord004() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_RECORD);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TARGET + "="
                + TestMediaStreamRecordingProfileConstants.ID);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TIME_SLICE + "=3600");
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.post(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(MediaStreamRecordingProfile.PARAM_URI), is(notNullValue()));
        // TODO URI比較
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の一時停止依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/pause?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPause() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_PAUSE);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の再開依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/resume?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testResume() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_RESUME);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の停止依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/stop?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testStop() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_STOP);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音のミュート依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/muteTrack?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testMuteTrack() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_MUTETRACK);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音のミュート依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/unmuteTrack?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testUnmuteTrack() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_UNMUTETRACK);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * 指定したスマートデバイスのカメラがサポートするオプションの一覧を取得するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaStreamRecording/options?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOptions001() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_OPTIONS);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        // TODO 各パラメータチェック
    }

    /**
     * 指定したスマートデバイスのカメラがサポートするオプションの一覧を取得するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediaStreamRecording/options?serviceId=xxxx&target=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetOptions002() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_OPTIONS);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TARGET + "="
                + TestMediaStreamRecordingProfileConstants.ID);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        // TODO 各パラメータチェック
    }

    /**
     * 指定したスマートデバイスのカメラにオプションを設定するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/options?serviceId=xxxx&target=xxxx&imageWidth=xxxx&imageHeight=xxxx&mimeType=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutOptions() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_OPTIONS);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_TARGET + "="
                + TestMediaStreamRecordingProfileConstants.ID);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_IMAGE_WIDTH);
        builder.append("=");
        builder.append(TestMediaStreamRecordingProfileConstants.IMAGE_WIDTH);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_IMAGE_HEIGHT);
        builder.append("=");
        builder.append(TestMediaStreamRecordingProfileConstants.IMAGE_HEIGHT);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_WIDTH);
        builder.append("=");
        builder.append(TestMediaStreamRecordingProfileConstants.PREVIEW_WIDTH);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_HEIGHT);
        builder.append("=");
        builder.append(TestMediaStreamRecordingProfileConstants.PREVIEW_HEIGHT);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_MAX_FRAME_RATE);
        builder.append("=");
        builder.append(TestMediaStreamRecordingProfileConstants.PREVIEW_MAX_FRAME_RATE);
        builder.append("&");
        builder.append(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE + "="
                + TestMediaStreamRecordingProfileConstants.MIME_TYPE);
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        // TODO 各パラメータチェック
    }

    /**
     * 写真撮影イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/onphoto?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testOnPhoto01() {
        try {
            registerEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_PHOTO);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 写真撮影イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediaStreamRecording/onphoto?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnPhoto02() {
        unregisterEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_PHOTO);
    }

    /**
     * 動画撮影または音声録音開始イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/onrecordingchange?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testOnRecording01() {
        try {
            registerEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_RECORDING_CHANGE);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 動画撮影または音声録音開始イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediaStreamRecording/onrecordingchange?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testOnRecording02() {
        unregisterEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_RECORDING_CHANGE);
    }

    /**
     * プレビュー開始テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediaStreamRecording/preview?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testPutPreview() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_PREVIEW);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        // TODO 各パラメータチェック
    }

    /**
     * プレビュー停止テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediaStreamRecording/preview?serviceId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testDeletePreview() {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + MediaStreamRecordingProfileConstants.ATTRIBUTE_PREVIEW);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * コールバック登録リクエストを送信する.
     * @param attribute コールバックの属性名
     * @throws JSONException JSONの解析に失敗した場合
     */
    private void registerEventCallback(final String attribute) throws JSONException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + attribute);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());


        DConnectResponseMessage response = mDConnectSDK.put(builder.toString(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * コールバック解除リクエストを送信する.
     * @param attribute コールバックの属性名
     */
    private void unregisterEventCallback(final String attribute) {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + attribute);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.delete(builder.toString());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * メディアIDで指定したファイルのURIを取得する.
     * @param mediaId メディアID
     * @return ファイルのURI
     */
    private String getFileURI(final String mediaId) {
        return DCONNECT_MANAGER_URI + "/files?uri=" + mediaId;
    }
}
