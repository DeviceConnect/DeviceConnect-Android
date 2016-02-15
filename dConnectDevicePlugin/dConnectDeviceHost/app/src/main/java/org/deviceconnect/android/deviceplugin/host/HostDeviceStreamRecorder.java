/*
 HostDeviceStreamRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


/**
 * Host Device Stream Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
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
