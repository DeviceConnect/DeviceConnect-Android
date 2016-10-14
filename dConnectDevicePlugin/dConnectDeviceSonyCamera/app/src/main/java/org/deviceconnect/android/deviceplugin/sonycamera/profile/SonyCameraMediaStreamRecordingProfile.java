/*
SonyCameraMediaStreamRecordingProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
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

/**
 * Sony Camera用 Media Stream Recording プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private final DConnectApi mGetMediaRecorderApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onGetMediaRecorder(request, response,
                getServiceID(request));
        }
    };

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

            mLogger.exiting(this.getClass().getName(), "onPutOnPhoto");
            return true;
        }
    };

    private final DConnectApi mPutPreviewApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onPutPreview(request, response,
                getTarget(request));
        }
    };

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

            mLogger.exiting(this.getClass().getName(), "onDeleteOnPhoto");
            return true;
        }
    };

    private final DConnectApi mDeletePreviewApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onDeletePreview(request, response,
                getTarget(request));
        }
    };

    private final DConnectApi mPostTakePhotoApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_TAKE_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onPostTakePhoto(request, response,
                getServiceID(request), getTarget(request));
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
}
