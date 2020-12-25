package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class Camera2BroadcasterProvider extends AbstractBroadcastProvider {
    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    public Camera2BroadcasterProvider(Context context, Camera2Recorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        if (broadcastURI.startsWith("srt://")) {
            return new Camera2SRTBroadcaster(mRecorder, broadcastURI);
        } else {
            return new Camera2RTMPBroadcaster(mRecorder, broadcastURI);
        }
    }
}
