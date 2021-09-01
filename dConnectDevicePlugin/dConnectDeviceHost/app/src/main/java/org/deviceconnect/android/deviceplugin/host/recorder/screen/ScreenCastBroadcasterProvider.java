package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class ScreenCastBroadcasterProvider extends AbstractBroadcastProvider {

    private final ScreenCastRecorder mRecorder;

    public ScreenCastBroadcasterProvider(Context context, ScreenCastRecorder recorder) {
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
            return new ScreenCastSRTBroadcaster(mRecorder, broadcastURI, name);
        } else if (broadcastURI.startsWith("rtmp://") || broadcastURI.startsWith("rtmps://")) {
            return new ScreenCastRTMPBroadcaster(mRecorder, broadcastURI, name);
        } else {
            return null;
        }
    }
}
