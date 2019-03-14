/*
SonyCameraMediaStreamRecordingProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraManager;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraPreview;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.sonycamera.service.SonyCameraService.DEVICE_NAME;

/**
 * Sony Camera用 Media Stream Recording プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraMediaStreamRecordingProfile extends MediaStreamRecordingProfile {
    /** ターゲットID. */
    private static final String TARGET_ID = "sonycamera";

    // GET /mediaStreamRecording/mediaRecorder
    private final DConnectApi mGetMediaRecorderApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onGetMediaRecorder(request, response);
        }
    };

    // POST /mediaStreamRecording/onPhoto
    private final DConnectApi mPutOnPhotoApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    // PUT /mediaStreamRecording/preview
    private final DConnectApi mPutPreviewApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPutPreview(request, response);
        }
    };

    // DELETE /mediaStreamRecording/onPhoto
    private final DConnectApi mDeleteOnPhotoApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else if (error == EventError.FAILED) {
                MessageUtils.setUnknownError(response, "Failed to uninsert event for db.");
            } else if (error == EventError.NOT_FOUND) {
                MessageUtils.setUnknownError(response, "Not found event.");
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    // DELETE /mediaStreamRecording/preview
    private final DConnectApi mDeletePreviewApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onDeletePreview(request, response);
        }
    };

    // POST /mediaStreamRecording/takePhoto
    private final DConnectApi mPostTakePhotoApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_TAKE_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPostTakePhoto(request, response);
        }
    };

    public SonyCameraMediaStreamRecordingProfile() {
        addApi(mGetMediaRecorderApi);
        addApi(mPutOnPhotoApi);
        addApi(mPutPreviewApi);
        addApi(mDeleteOnPhotoApi);
        addApi(mDeletePreviewApi);
        addApi(mPostTakePhotoApi);
    }

    /**
     * カメラの情報を取得します.
     *
     * @param request リクエスト
     * @param response レスポンス
     * @return 即座にレスポンスする場合はtrue、それ以外はfalse
     */
    private boolean onGetMediaRecorder(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        manager.getCameraState(new SonyCameraManager.OnCameraStateListener() {
            @Override
            public void onState(final String state, final int[] size) {
                List<Bundle> recorders = new ArrayList<>();

                Bundle recorder = new Bundle();
                recorder.putString("id", TARGET_ID);
                recorder.putString("name", DEVICE_NAME);
                recorder.putString("state", state);
                recorder.putString("mimeType", "image/jpeg");
                if (size != null) {
                    recorder.putInt("imageWidth", size[0]);
                    recorder.putInt("imageHeight", size[1]);
                }
                recorders.add(recorder);

                response.putExtra("recorders", recorders.toArray(new Bundle[recorders.size()]));
                setResult(response, DConnectMessage.RESULT_OK);

                sendResponse(response);
            }

            @Override
            public void onError() {
                MessageUtils.setNotSupportAttributeError(response);
                sendResponse(response);
            }
        });
        return false;
    }

    /**
     * プレビューを撮影します.
     *
     * @param request リクエスト
     * @param response レスポンス
     *
     * @return プレビューの開始ができた場合はtrue、それ以外はfalse
     */
    private boolean onPutPreview(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String target = getTarget(request);
        Long timeSlice = getTimeSlice(request);
        if (timeSlice == null) {
            timeSlice = 100L;
        }

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        if (target != null && !target.equals(TARGET_ID)) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        manager.startPreview(timeSlice.intValue(), new SonyCameraPreview.OnPreviewListener() {
            @Override
            public void onPreviewServer(final String url) {
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra("uri", url);
                sendResponse(response);
            }

            @Override
            public void onError() {
                MessageUtils.setIllegalDeviceStateError(response);
                sendResponse(response);
            }

            @Override
            public void onComplete() {
            }
        });
        return false;
    }

    /**
     * プレビューを停止します.
     *
     * @param request リクエスト
     * @param response レスポンス
     * @return result
     */
    private boolean onDeletePreview(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String target = getTarget(request);

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        if (target != null && !target.equals(TARGET_ID)) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        if (!manager.isPreview()) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not running a preview.");
            return true;
        }

        manager.stopPreview();

        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    /**
     * 写真を撮影します.
     *
     * @param request リクエスト
     * @param response レスポンス
     * @return 即座にレスポンスを返す場合はtrue、それ以外はfalse
     */
    private boolean onPostTakePhoto(final Intent request, final Intent response) {
        final String serviceId = getServiceID(request);
        final String target = getTarget(request);

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        if (target != null && !target.equals(TARGET_ID)) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        if (manager.isRecording()) {
            // 撮影中は、さらに撮影できないのでエラーを返す
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }

        manager.takePicture(new SonyCameraManager.OnTakePictureListener() {
            @Override
            public void onSuccess(final String postImageUrl) {
                response.putExtra("uri", postImageUrl);
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onError() {
                MessageUtils.setIllegalDeviceStateError(response);
                sendResponse(response);
            }
        });

        return false;
    }

    private SonyCameraManager getSonyCameraManager() {
        return ((SonyCameraDeviceService) getContext()).getSonyCameraManager();
    }
}
