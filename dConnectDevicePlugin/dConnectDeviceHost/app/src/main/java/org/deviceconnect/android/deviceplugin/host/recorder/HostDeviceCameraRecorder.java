package org.deviceconnect.android.deviceplugin.host.recorder;

public interface HostDeviceCameraRecorder {

    void takePhoto(OnCameraEventListener listener);

    interface OnCameraEventListener {
        void onTakePhoto(String uri, String filePath);
        void onFailedTakePhoto();
    }
}
