/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

public interface HostDevicePhotoRecorder {

    void takePhoto(OnPhotoEventListener listener);

    boolean isBack();

    void turnOnFlashLight();

    void turnOffFlashLight();

    boolean isFlashLightState();

    boolean isUseFlashLight();

    interface OnPhotoEventListener {
        void onTakePhoto(String uri, String filePath);
        void onFailedTakePhoto(String errorMessage);
    }
}
