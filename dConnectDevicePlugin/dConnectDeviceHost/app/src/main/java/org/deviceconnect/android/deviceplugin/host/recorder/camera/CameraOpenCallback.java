/*
 CameraOpenCallback.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.hardware.camera2.CameraDevice;
import android.support.annotation.NonNull;

interface CameraOpenCallback {

    void onOpen(@NonNull CameraDevice camera, boolean isNew);

    void onError(@NonNull Exception ex);
}
