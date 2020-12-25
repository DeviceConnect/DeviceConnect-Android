package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class ScreenCastBroadcasterProvider extends AbstractBroadcastProvider {

    private ScreenCastRecorder mRecorder;

    public ScreenCastBroadcasterProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        if (broadcastURI.startsWith("srt://")) {
            return new ScreenCastSRTBroadcaster(mRecorder, broadcastURI);
        } else {
            return new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI);
        }
    }
}
