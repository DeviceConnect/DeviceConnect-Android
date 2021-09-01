package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTBroadcaster;

public class AudioSRTBroadcaster extends AbstractSRTBroadcaster {
    public AudioSRTBroadcaster(HostAudioRecorder recorder, String broadcastURI, String name) {
        super(recorder, broadcastURI, name);
    }
}
