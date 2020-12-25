package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class AudioBroadcasterProvider extends AbstractBroadcastProvider {

    public AudioBroadcasterProvider(Context context, HostAudioRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public Broadcaster createBroadcaster(String broadcastURI) {
        return null;
    }
}
