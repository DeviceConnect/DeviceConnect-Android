package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class AudioBroadcasterProvider extends AbstractBroadcastProvider {

    private final HostAudioRecorder mRecorder;

    public AudioBroadcasterProvider(Context context, HostAudioRecorder recorder) {
        super(context, recorder);
        mRecorder = recorder;
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        if (broadcastURI.startsWith("srt://")) {
            return new AudioSRTBroadcaster(mRecorder, broadcastURI);
        } else if (broadcastURI.startsWith("rtmp://") || broadcastURI.startsWith("rtmps://")) {
            return new AudioRTMPBroadcaster(mRecorder, broadcastURI);
        } else {
            return null;
        }
    }
}
