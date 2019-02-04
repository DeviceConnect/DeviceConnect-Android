/*
 SurfaceRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.view.Surface;

import java.io.File;

interface SurfaceRecorder {

    Surface getInputSurface();

    void start(final OnRecordingStartListener listener);

    void stop(final OnRecordingStopListener listener);

    void release();

    File getOutputFile();

    interface OnRecordingStartListener {
        void onRecordingStart();
        void onRecordingStartError(Throwable e);
    }

    interface OnRecordingStopListener {
        void onRecordingStop();
        void onRecordingStopError(Throwable e);
    }
}
