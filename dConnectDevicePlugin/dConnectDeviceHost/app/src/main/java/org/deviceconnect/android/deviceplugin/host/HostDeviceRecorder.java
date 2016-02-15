/*
 HostDeviceRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


/**
 * Host Device Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorder {

    String getId();

    String getName();

    String getMimeType();

    RecorderState getState();

    enum RecorderState {
        INACTTIVE,
        PAUSED,
        RECORDING
    }

    enum CameraFacing {

        BACK("back"),
        FRONT("front"),
        UNKNOWN("unknown");

        String mName;

        CameraFacing(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

}
