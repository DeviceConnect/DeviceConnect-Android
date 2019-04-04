/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.os.Handler;

public interface HostDevicePhotoRecorder {

    void takePhoto(OnPhotoEventListener listener);

    boolean isBack();

    void turnOnFlashLight(TurnOnFlashLightListener listener, Handler handler);

    void turnOffFlashLight(TurnOffFlashLightListener listener, Handler handler);

    boolean isFlashLightState();

    boolean isUseFlashLight();

    interface OnPhotoEventListener {
        void onTakePhoto(String uri, String filePath, String mimeType);
        void onFailedTakePhoto(String errorMessage);
    }

    interface TurnOnFlashLightListener {
        void onRequested();
        void onTurnOn();
        void onError(Error error);
    }

    interface TurnOffFlashLightListener {
        void onRequested();
        void onTurnOff();
        void onError(Error error);
    }

    enum Error {
        UNSUPPORTED,
        FATAL_ERROR
    }
}
