/*
 SurfaceRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.view.Surface;

import java.io.File;

/**
 * 指定された Surface を録画するためのインターフェース.
 */
public interface SurfaceRecorder {

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
