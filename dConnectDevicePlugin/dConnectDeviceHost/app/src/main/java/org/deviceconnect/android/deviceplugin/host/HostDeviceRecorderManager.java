package org.deviceconnect.android.deviceplugin.host;


import java.util.ArrayList;
import java.util.List;

public class HostDeviceRecorderManager {

    private final List<HostDeviceRecorder> mRecorders = new ArrayList<>();

    public List<HostDeviceRecorder> getRecorders() {
        return mRecorders;
    }

    public HostDevicePhotoRecorder getPhotoRecorder(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null.");
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePhotoRecorder) {
                return (HostDevicePhotoRecorder) recorder;
            }
        }
        return null;
    }

    public HostDeviceStreamRecorder getStreamRecorder(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null.");
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceStreamRecorder) {
                return (HostDeviceStreamRecorder) recorder;
            }
        }
        return null;
    }

}
