package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.Broadcaster;

class UvcBroadcasterProvider extends AbstractBroadcastProvider {
    /**
     * カメラを操作するレコーダ.
     */
    private final UvcH264Recorder mRecorder;

    public UvcBroadcasterProvider(Context context, UvcH264Recorder recorder) {
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