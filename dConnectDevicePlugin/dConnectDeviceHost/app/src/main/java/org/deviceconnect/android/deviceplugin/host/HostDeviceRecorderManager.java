package org.deviceconnect.android.deviceplugin.host;


public interface HostDeviceRecorderManager {

    HostDeviceRecorder[] getRecorders();

    HostDevicePhotoRecorder getPhotoRecorder();

    HostDevicePhotoRecorder getPhotoRecorder(final String id);

    HostDeviceStreamRecorder getStreamRecorder(final String id);

}
