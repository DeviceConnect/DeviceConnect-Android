package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class Camera2BroadcasterProvider extends AbstractBroadcastProvider {
    /**
     * カメラを操作するレコーダ.
     */
    private final Camera2Recorder mRecorder;

    public Camera2BroadcasterProvider(Context context, Camera2Recorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        String name = null;
        for (String n : mRecorder.getSettings().getBroadcasterList()) {
            HostMediaRecorder.StreamingSettings s = mRecorder.getSettings().getBroadcaster(n);
            if (broadcastURI.equals(s.getBroadcastURI())) {
                name = n;
            }
        }

        if (broadcastURI.startsWith("srt://")) {
            return new Camera2SRTBroadcaster(mRecorder, broadcastURI, name);
        } else if (broadcastURI.startsWith("rtmp://") || broadcastURI.startsWith("rtmps://")) {
            return new Camera2RTMPBroadcaster(mRecorder, broadcastURI, name);
        } else {
            return null;
        }
    }
}
