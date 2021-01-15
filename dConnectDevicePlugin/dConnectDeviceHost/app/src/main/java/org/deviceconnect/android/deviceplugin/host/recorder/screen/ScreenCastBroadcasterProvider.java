package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class ScreenCastBroadcasterProvider extends AbstractBroadcastProvider {

    private final ScreenCastRecorder mRecorder;

    public ScreenCastBroadcasterProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        if (broadcastURI.startsWith("srt://")) {
            return new ScreenCastSRTBroadcaster(mRecorder, broadcastURI);
        } else if (broadcastURI.startsWith("rtmp://")) {
            return new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI);
        } else {
            return null;
        }
    }
}
