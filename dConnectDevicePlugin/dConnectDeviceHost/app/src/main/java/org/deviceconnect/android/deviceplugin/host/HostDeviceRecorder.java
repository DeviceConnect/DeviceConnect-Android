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

    String[] getSupportedMimeTypes();

    RecorderState getState();

    boolean usesCamera();

    int getCameraId();

    enum RecorderState {
        INACTTIVE,
        PAUSED,
        RECORDING
    }

    enum CameraFacing {

        BACK("back"),
        FRONT("front"),
        UNKNOWN("unknown");

        private final String mName;

        CameraFacing(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    class PictureSize {

        private final int mWidth;
        private final int mHeight;

        public PictureSize(final int w, final int h) {
            mWidth = w;
            mHeight = h;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

    }

}
