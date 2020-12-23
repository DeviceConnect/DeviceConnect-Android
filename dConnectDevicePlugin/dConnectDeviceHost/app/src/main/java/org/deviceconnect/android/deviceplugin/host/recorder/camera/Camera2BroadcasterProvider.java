package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class Camera2BroadcasterProvider extends AbstractBroadcastProvider {
    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    public Camera2BroadcasterProvider(Camera2Recorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        return new Camera2RTMPBroadcaster(mRecorder, broadcastURI);
    }
}
