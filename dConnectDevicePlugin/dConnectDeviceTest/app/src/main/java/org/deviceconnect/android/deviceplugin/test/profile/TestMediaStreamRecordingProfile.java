/*
 TestMediaStreamRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * JUnit用テストデバイスプラグイン、MediaStreamRecordingプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    /**
     * カメラID.
     */
    private static final String ID = "test_camera_id";

    /**
     * カメラの名前.
     */
    private static final String NAME = "test_camera_name";

    /**
     * カメラの状態.
     */
    private static final RecorderState STATE = RecorderState.INACTIVE;

    /**
     * レコーダの横幅.
     */
    private static final int IMAGE_WIDTH = 1920;

    /**
     * レコーダの縦幅.
     */
    private static final int IMAGE_HEIGHT = 1080;

    /**
     * プレビューの横幅.
     */
    private static final int PREVIEW_WIDTH = 640;

    /**
     * プレビューの縦幅.
     */
    private static final int PREVIEW_HEIGHT = 480;

    /**
     * プレビューの最大フレームレート.
     */
    private static final double PREVIEW_MAX_FRAME_RATE = 30.0d;

    /**
     * レコーダの状態.
     */
    private static final RecordingState MEDIA_STATUS = RecordingState.RECORDING;

    /**
     * レコーダのエンコードするMIMEタイプ.
     */
    private static final String MIME_TYPE = "video/mp4";

    /**
     * カメラ設定.
     */
    private static final String CONFIG = "test_config";

    /**
     * 撮影した写真のURI.
     */
    private static final String URI = "content://test/test.mp4";

    /**
     * メディアID.
     */
    private static final String PATH = "test.mp4";

    /**
     * プレビュー動画配信URI.
     */
    private static final String PREVIEW_URI = "http://localhost:9000/preview";

    /**
     * 音声配信URI.
     */
    private static final String AUDIO_URI = "http://localhost:9000/audio";

    /**
     * 音声のチャンネル数.
     */
    private static final int AUDIO_CHANNELS = 1;

    /**
     * 音声のサンプルレート.
     */
    private static final int AUDIO_SAMPLE_RATE = 0;

    /**
     * 音声のサンプルサイズ.
     */
    private static final int AUDIO_SAMPLE_SIZE = 16;

    /**
     * 音声のブロックサイズ.
     */
    private static final int AUDIO_BLOCK_SIZE = 8;

    /**
     * サービスIDをチェックする.
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkserviceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            List<Bundle> recorders = new LinkedList<Bundle>();
            Bundle recorder = new Bundle();
            setRecorderId(recorder, ID);
            setRecorderName(recorder, NAME);
            setRecorderState(recorder, STATE);
            setRecorderImageWidth(recorder, IMAGE_WIDTH);
            setRecorderImageHeight(recorder, IMAGE_HEIGHT);
            setRecorderPreviewWidth(recorder, PREVIEW_WIDTH);
            setRecorderPreviewHeight(recorder, PREVIEW_HEIGHT);
            setRecorderPreviewMaxFrameRate(recorder, PREVIEW_MAX_FRAME_RATE);
            Bundle audio = new Bundle();
            setAudioChannels(audio, AUDIO_CHANNELS);
            setAudioSampleRate(audio, AUDIO_SAMPLE_RATE);
            setAudioSampleSize(audio, AUDIO_SAMPLE_SIZE);
            setAudioBlockSize(audio, AUDIO_BLOCK_SIZE);
            setRecorderAudio(recorder, audio);
            setRecorderMIMEType(recorder, MIME_TYPE);
            setRecorderConfig(recorder, CONFIG);
            recorders.add(recorder);
            setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
        }
        return true;
    }

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setUri(response, URI);
        }
        return true;
    }

    @Override
    protected boolean onPostRecord(final Intent request, final Intent response, final String serviceId, 
            final String target, final Long timeslice) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setUri(response, URI);
        }
        return true;
    }

    @Override
    protected boolean onPutPause(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutResume(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutStop(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutMuteTrack(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutUnmuteTrack(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOptions(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            // imageSizes
            Bundle[] imageSizes = new Bundle[1];
            imageSizes[0] = new Bundle();
            setWidth(imageSizes[0], IMAGE_WIDTH);
            setHeight(imageSizes[0], IMAGE_HEIGHT);
            setImageSizes(response, imageSizes);

            // previewSizes
            Bundle[] previewSizes = new Bundle[1];
            previewSizes[0] = new Bundle();
            setWidth(previewSizes[0], PREVIEW_WIDTH);
            setHeight(previewSizes[0], PREVIEW_HEIGHT);
            setPreviewSizes(response, previewSizes);

            // mimeType
            setMIMEType(response, new String[] {MIME_TYPE});
        }
        return true;
    }

    @Override
    protected boolean onPutOptions(final Intent request, final Intent response, final String serviceId, 
            final String target, final Integer imageWidth, final Integer imageHeight,
            final Integer previewWidth, final Integer previewHeight, final Double previewMaxFrameRate,
            final String mimeType) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (TextUtils.isEmpty(target) || TextUtils.isEmpty(mimeType)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnPhoto(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_PHOTO);
            Bundle photo = new Bundle();
            setPath(photo, PATH);
            setMIMEType(photo, MIME_TYPE);
            setPhoto(message, photo);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onPutOnRecordingChange(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_RECORDING_CHANGE);
            Bundle media = new Bundle();
            setStatus(media, MEDIA_STATUS);
            setPath(media, PATH);
            setMIMEType(media, MIME_TYPE);
            setMedia(message, media);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onPutOnDataAvailable(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_DATA_AVAILABLE);
            Bundle media = new Bundle();
            setUri(media, URI);
            setMIMEType(media, MIME_TYPE);
            setMedia(message, media);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnPhoto(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnRecordingChange(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDataAvailable(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response, final String serviceId,
                                   final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setUri(response, PREVIEW_URI);
            Bundle audio = new Bundle();
            setAudioUri(audio, AUDIO_URI);
            setAudio(response, audio);
        }
        return true;
    }

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response, final String serviceId,
                                      final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }
}
