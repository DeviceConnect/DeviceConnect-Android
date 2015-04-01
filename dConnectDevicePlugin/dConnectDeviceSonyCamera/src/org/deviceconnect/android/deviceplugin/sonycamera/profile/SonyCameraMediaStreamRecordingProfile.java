/*
SonyCameraMediaStreamRecordingProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * Sony Camera用 Media Stream Recording プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response, final String serviceId) {
        return ((SonyCameraDeviceService) getContext()).onGetMediaRecorder(request, response, serviceId);
    }

    @Override
    protected boolean onPutOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID:" + serviceId);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
        }

        mLogger.exiting(this.getClass().getName(), "onPutOnPhoto");
        return true;
    }

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response, final String serviceId) {
        return ((SonyCameraDeviceService) getContext()).onPutPreview(request, response);
    }

    @Override
    protected boolean onDeleteOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
        } else {
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
        }

        mLogger.exiting(this.getClass().getName(), "onDeleteOnPhoto");
        return true;
    }

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response, final String serviceId) {
        return ((SonyCameraDeviceService) getContext()).onDeletePreview(request, response);
    }

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response, final String serviceId,
            final String target) {
        return ((SonyCameraDeviceService) getContext()).onPostTakePhoto(request, response, serviceId, target);
    }
}
