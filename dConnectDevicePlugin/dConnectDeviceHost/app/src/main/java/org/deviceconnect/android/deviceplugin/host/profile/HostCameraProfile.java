/*
 HostCameraProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Camera Profile. (Experimental)
 *
 * @author NTT DOCOMO, INC.
 */
public class HostCameraProfile extends DConnectProfile {

    private HostMediaRecorderManager mRecorderManager;
    @Override
    public String getProfileName() {
        return "camera";
    }

    public HostCameraProfile(final HostMediaRecorderManager recorderManager) {
        mRecorderManager = recorderManager;
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "options";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = request.getStringExtra("target");

                Camera2Recorder recorder = getCameraRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "camera is not found.");
                    return true;
                }
                Bundle photo = new Bundle();
                photo.putString("whiteBalance", recorder.getWhiteBalance());
                response.putExtra("photo", photo);
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "options";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = request.getStringExtra("target");
                String whiteBalance = request.getStringExtra("whiteBalance");

                Camera2Recorder recorder = getCameraRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "camera is not found.");
                    return true;
                }
                if (!TextUtils.isEmpty(whiteBalance)) {
                    recorder.setWhiteBalance(whiteBalance);
                }
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    private Camera2Recorder getCameraRecorder(final String target) {
        if (mRecorderManager == null) {
            return null;
        }
        HostMediaRecorder recorder = mRecorderManager.getRecorder(target);
        if (recorder instanceof Camera2Recorder) {
            return (Camera2Recorder) recorder;
        }
        return null;
    }
}
