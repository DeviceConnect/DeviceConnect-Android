/*
 HostDeviceStreamRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


/**
 * Host Device Stream Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceStreamRecorder extends HostDeviceRecorder {

    boolean canPauseRecording();

    void startRecording(RecordingListener listener);

    void stopRecording();

    void pauseRecording();

    void resumeRecording();

    interface RecordingListener {
        void onRecorded(HostDeviceStreamRecorder recorder, String fileName);
        void onFailed(HostDeviceStreamRecorder recorder, String errorMessage);
    }

}
