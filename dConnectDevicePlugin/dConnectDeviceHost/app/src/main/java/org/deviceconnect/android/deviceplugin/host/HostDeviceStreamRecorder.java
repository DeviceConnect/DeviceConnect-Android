package org.deviceconnect.android.deviceplugin.host;


public interface HostDeviceStreamRecorder extends HostDeviceRecorder {

    boolean canPause();

    void start();

    void stop();

    void pause();

    void resume();

    void setRecordingListener(RecordingListener listener);

    interface RecordingListener {
        void onStartRecording(HostDeviceRecorder recorder);
        void onPauseRecording(HostDeviceRecorder recorder);
        void onResumeRecording(HostDeviceRecorder recorder);
        void onStopRecording(HostDeviceRecorder recorder);
    }

}
