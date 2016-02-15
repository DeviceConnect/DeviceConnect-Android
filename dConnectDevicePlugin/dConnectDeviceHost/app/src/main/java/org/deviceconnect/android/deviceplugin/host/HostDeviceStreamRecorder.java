package org.deviceconnect.android.deviceplugin.host;


public interface HostDeviceStreamRecorder extends HostDeviceRecorder {

    boolean canPause();

    void start(RecordingListener listener);

    void stop();

    void pause();

    void resume();

    interface RecordingListener {
        void onRecorded(HostDeviceRecorder recorder, String fileName);
        void onFailed(HostDeviceRecorder recorder, String errorMessage);
    }

}
