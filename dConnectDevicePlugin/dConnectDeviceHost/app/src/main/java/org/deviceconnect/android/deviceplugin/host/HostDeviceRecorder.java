package org.deviceconnect.android.deviceplugin.host;


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

}
