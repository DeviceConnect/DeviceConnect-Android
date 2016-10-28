package org.deviceconnect.android.deviceplugin.host.recorder;

public interface HostDevicePhotoRecorder {

    void takePhoto(OnPhotoEventListener listener);

    interface OnPhotoEventListener {
        void onTakePhoto(String uri, String filePath);
        void onFailedTakePhoto();
    }
}
