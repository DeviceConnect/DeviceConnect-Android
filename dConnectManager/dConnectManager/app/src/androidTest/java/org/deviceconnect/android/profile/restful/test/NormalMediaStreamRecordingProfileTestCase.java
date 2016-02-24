/*
 NormalMediaStreamRecordingProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.test.plugin.profile.TestMediaStreamRecordingProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;


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
     * Path: /mediastream_recording/mediarecorder?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));

            JSONArray recorders = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_RECORDERS);
            assertEquals(1, recorders.length());
            JSONObject recorder = recorders.getJSONObject(0);
            assertEquals(TestMediaStreamRecordingProfileConstants.ID,
                recorder.getString(MediaStreamRecordingProfileConstants.PARAM_ID));
            assertEquals(TestMediaStreamRecordingProfileConstants.NAME,
                recorder.getString(MediaStreamRecordingProfileConstants.PARAM_NAME));
            assertEquals(TestMediaStreamRecordingProfileConstants.STATE,
                recorder.getString(MediaStreamRecordingProfileConstants.PARAM_STATE));
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_WIDTH,
                recorder.getInt(MediaStreamRecordingProfileConstants.PARAM_IMAGE_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_HEIGHT,
                recorder.getInt(MediaStreamRecordingProfileConstants.PARAM_IMAGE_HEIGHT));
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_WIDTH,
                recorder.getInt(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_HEIGHT,
                recorder.getInt(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_HEIGHT));
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_MAX_FRAME_RATE,
                recorder.getDouble(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_MAX_FRAME_RATE));
            assertEquals(TestMediaStreamRecordingProfileConstants.CONFIG,
                recorder.getString(MediaStreamRecordingProfileConstants.PARAM_CONFIG));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して写真撮影依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/takephoto?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して写真撮影依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/takephoto?serviceId=xxxx&target=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/record?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/record?serviceId=xxxx&target=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/record?serviceId=xxxx&timeslice=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の開始依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /mediastream_recording/record?serviceId=xxxx&target=xxxx&timeslice=xxxx
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
        try {
            HttpUriRequest request = new HttpPost(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI),
                    root.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の一時停止依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/pause?serviceId=xxxx&mediaid=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の再開依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/resume?serviceId=xxxx&mediaid=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音の停止依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/stop?serviceId=xxxx&mediaid=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音のミュート依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/mutetrack?serviceId=xxxx&mediaid=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスに対して動画撮影または音声録音のミュート依頼を送信するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/unmutetrack?serviceId=xxxx&mediaid=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスのカメラがサポートするオプションの一覧を取得するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediastream_recording/options?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));

            // imageSizes
            JSONArray imageSizes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_IMAGE_SIZES);
            assertEquals(1, imageSizes.length());
            JSONObject imageSize = imageSizes.getJSONObject(0);
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_WIDTH,
                imageSize.getInt(MediaStreamRecordingProfileConstants.PARAM_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_HEIGHT,
                imageSize.getInt(MediaStreamRecordingProfileConstants.PARAM_HEIGHT));

            // previewSizes
            JSONArray previewSizes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_SIZES);
            assertEquals(1, previewSizes.length());
            JSONObject previewSize = previewSizes.getJSONObject(0);
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_WIDTH,
                previewSize.getInt(MediaStreamRecordingProfileConstants.PARAM_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_HEIGHT,
                previewSize.getInt(MediaStreamRecordingProfileConstants.PARAM_HEIGHT));

            // mimeType
            JSONArray mimeTypes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE);
            assertEquals("video/mp4", mimeTypes.get(0));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスのカメラがサポートするオプションの一覧を取得するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /mediastream_recording/options?serviceId=xxxx&target=xxxx
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
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));

            // imageSizes
            JSONArray imageSizes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_IMAGE_SIZES);
            assertEquals(1, imageSizes.length());
            JSONObject imageSize = imageSizes.getJSONObject(0);
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_WIDTH,
                imageSize.getInt(MediaStreamRecordingProfileConstants.PARAM_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.IMAGE_HEIGHT,
                imageSize.getInt(MediaStreamRecordingProfileConstants.PARAM_HEIGHT));

            // previewSizes
            JSONArray previewSizes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_PREVIEW_SIZES);
            assertEquals(1, previewSizes.length());
            JSONObject previewSize = previewSizes.getJSONObject(0);
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_WIDTH,
                previewSize.getInt(MediaStreamRecordingProfileConstants.PARAM_WIDTH));
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_HEIGHT,
                previewSize.getInt(MediaStreamRecordingProfileConstants.PARAM_HEIGHT));

            // mimeType
            JSONArray mimeTypes = root.getJSONArray(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE);
            assertEquals("video/mp4", mimeTypes.get(0));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 指定したスマートデバイスのカメラにオプションを設定するテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/options?serviceId=xxxx&target=xxxx&imageWidth=xxxx&imageHeight=xxxx&mimeType=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);
            assertEquals(DConnectMessage.RESULT_OK,
                    root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 写真撮影イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/onphoto?serviceId=xxxx&session_key=xxxx
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
            JSONObject event = registerEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_PHOTO);
            JSONObject photo = event.getJSONObject(MediaStreamRecordingProfileConstants.PARAM_PHOTO);
            assertEquals(TestMediaStreamRecordingProfileConstants.PATH, 
                    photo.getString(MediaStreamRecordingProfileConstants.PARAM_PATH));
            assertEquals(TestMediaStreamRecordingProfileConstants.MIME_TYPE, 
                    photo.getString(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 写真撮影イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediastream_recording/onphoto?serviceId=xxxx&session_key=xxxx
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
     * Path: /mediastream_recording/onrecordingchange?serviceId=xxxx&session_key=xxxx
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
            JSONObject event =
                    registerEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_RECORDING_CHANGE);
            JSONObject media = event.getJSONObject(MediaStreamRecordingProfileConstants.PARAM_MEDIA);
            assertEquals(TestMediaStreamRecordingProfileConstants.STATUS, 
                    media.getString(MediaStreamRecordingProfileConstants.PARAM_STATUS));
            assertEquals(TestMediaStreamRecordingProfileConstants.PATH, 
                    media.getString(MediaStreamRecordingProfileConstants.PARAM_PATH));
            assertEquals(TestMediaStreamRecordingProfileConstants.MIME_TYPE, 
                    media.getString(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 動画撮影または音声録音開始イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediastream_recording/onrecordingchange?serviceId=xxxx&session_key=xxxx
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
     * 動画撮影または音声録音の一定時間経過イベントのコールバック登録テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/ondeviceavailable?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testOnDataAvailable01() {
        try {
            @SuppressWarnings("deprecation")
            JSONObject event = registerEventCallback(
                     MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_DATA_AVAILABLE);
            JSONObject media = event.getJSONObject(MediaStreamRecordingProfileConstants.PARAM_MEDIA);
            assertEquals(getFileURI(TestMediaStreamRecordingProfileConstants.URI), 
                    media.getString(MediaStreamRecordingProfileConstants.PARAM_URI));
            assertEquals(TestMediaStreamRecordingProfileConstants.MIME_TYPE, 
                    media.getString(MediaStreamRecordingProfileConstants.PARAM_MIME_TYPE));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 動画撮影または音声録音の一定時間経過イベントのコールバック解除テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediastream_recording/ondeviceavailable?serviceId=xxxx&session_key=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @SuppressWarnings("deprecation")
	@Test
	public void testOnDataAvailable02() {
        unregisterEventCallback(MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_DATA_AVAILABLE);
    }

    /**
     * プレビュー開始テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /mediastream_recording/preview?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpPut(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);

            assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));

            // uri
            String uri = root.getString(MediaStreamRecordingProfileConstants.PARAM_URI);
            assertEquals(TestMediaStreamRecordingProfileConstants.PREVIEW_URI, uri);

            // audio
            JSONObject audio = root.getJSONObject(MediaStreamRecordingProfileConstants.PARAM_AUDIO);

            // audio.uri
            String audioUri = audio.getString(MediaStreamRecordingProfileConstants.PARAM_URI);
            assertEquals(TestMediaStreamRecordingProfileConstants.AUDIO_URI, audioUri);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * プレビュー停止テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /mediastream_recording/preview?serviceId=xxxx
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
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertNotNull("root is null.", root);

            assertEquals(DConnectMessage.RESULT_OK,
                root.getInt(DConnectMessage.EXTRA_RESULT));
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * コールバック登録リクエストを送信する.
     * @param attribute コールバックの属性名
     * @return 受信したイベント
     * @throws JSONException JSONの解析に失敗した場合
     */
    private JSONObject registerEventCallback(final String attribute) throws JSONException {
        StringBuilder builder = new StringBuilder();
        builder.append(DCONNECT_MANAGER_URI);
        builder.append("/" + MediaStreamRecordingProfileConstants.PROFILE_NAME);
        builder.append("/" + attribute);
        builder.append("?");
        builder.append(DConnectProfileConstants.PARAM_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        HttpUriRequest request = new HttpPut(builder.toString());
        JSONObject root = sendRequest(request);
        assertResultOK(root);
        JSONObject event = waitForEvent();
        assertNotNull("event is null.", event);
        return event;
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
        builder.append(DConnectProfileConstants.PARAM_SESSION_KEY + "=" + getClientId());
        builder.append("&");
        builder.append(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN + "=" + getAccessToken());
        try {
            HttpUriRequest request = new HttpDelete(builder.toString());
            JSONObject root = sendRequest(request);
            assertResultOK(root);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
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
