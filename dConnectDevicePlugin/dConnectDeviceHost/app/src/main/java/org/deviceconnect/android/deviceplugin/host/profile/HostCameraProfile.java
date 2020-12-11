/*
 HostCameraProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.hardware.camera2.CameraMetadata;
import android.os.Bundle;

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

                WhiteBalance wb = WhiteBalance.valueOf(recorder.getSettings().getPreviewWhiteBalance());
                if (wb != null) {
                    photo.putString("whiteBalance", wb.getName());
                }
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

                WhiteBalance wb = WhiteBalance.nameOf(whiteBalance);
                if (wb != null) {
                    recorder.getSettings().setPreviewWhiteBalance(wb.getValue());
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

    public enum WhiteBalance {
        AUTO("auto", CameraMetadata.CONTROL_AWB_MODE_AUTO),
        INCANDESCENT("incandescent", CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT),
        FLUORESCENT("fluorescent", CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT),
        WARM_FLUORESCENT("warm-fluorescent", CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT),
        DAYLIGHT("daylight", CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT),
        CLOUDY_DAYLIGHT("cloudy-daylight", CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT),
        TWILIGHT("twilight", CameraMetadata.CONTROL_AWB_MODE_TWILIGHT),
        SHADE("shade", CameraMetadata.CONTROL_AWB_MODE_SHADE),
        UNKNOWN("Unknown", -1);

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * カメラの番号.
         */
        private final int mValue;

        WhiteBalance(String name, int value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public int getValue() {
            return mValue;
        }

        public static WhiteBalance nameOf(String name) {
            for (WhiteBalance wb : WhiteBalance.values()) {
                if (wb.mName.equals(name)) {
                    return wb;
                }
            }
            return null;
        }

        public static WhiteBalance valueOf(int value) {
            for (WhiteBalance wb : WhiteBalance.values()) {
                if (wb.mValue == value) {
                    return wb;
                }
            }
            return null;
        }
    }
}
