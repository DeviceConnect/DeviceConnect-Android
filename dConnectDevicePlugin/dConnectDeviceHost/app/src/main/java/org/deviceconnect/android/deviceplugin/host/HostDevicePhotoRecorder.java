package org.deviceconnect.android.deviceplugin.host;


public interface HostDevicePhotoRecorder extends HostDeviceRecorder {

    void takePhoto(OnTakePhotoListener listener);

    interface OnTakePhotoListener {

        void onTakePhoto(String uri, String filePath);

        void onFailedTakePhoto();

    }

}
