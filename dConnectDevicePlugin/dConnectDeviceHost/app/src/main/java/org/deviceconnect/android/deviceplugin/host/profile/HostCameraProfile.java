package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

public class HostCameraProfile extends DConnectProfile {

    @Override
    public String getProfileName() {
        return "camera";
    }

    public HostCameraProfile() {

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
                recorder.setWhiteBalance(whiteBalance);
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    private Camera2Recorder getCameraRecorder(final String target) {
        HostDeviceService plugin = (HostDeviceService) getContext();
        HostDeviceRecorderManager recorderManager = plugin.getRecorderManager();
        if (recorderManager == null) {
            return null;
        }
        HostDeviceRecorder recorder = recorderManager.getRecorder(target);
        if (recorder instanceof Camera2Recorder) {
            return (Camera2Recorder) recorder;
        }
        return null;
    }
}
