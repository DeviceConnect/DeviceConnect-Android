package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.Broadcaster;

public class UvcBroadcasterProvider extends AbstractBroadcastProvider {
    /**
     * カメラを操作するレコーダ.
     */
    private final UvcRecorder mRecorder;

    public UvcBroadcasterProvider(Context context, UvcRecorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        if (broadcastURI.startsWith("srt://")) {
            return new UvcSRTBroadcaster(mRecorder, broadcastURI);
        } else if (broadcastURI.startsWith("rtmp://") || broadcastURI.startsWith("rtmps://")) {
            return new UvcRTMPBroadcaster(mRecorder, broadcastURI);
        } else {
            return null;
        }
    }
}