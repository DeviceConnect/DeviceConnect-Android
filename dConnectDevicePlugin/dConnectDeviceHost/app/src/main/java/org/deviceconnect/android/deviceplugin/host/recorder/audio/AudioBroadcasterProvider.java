package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;

public class AudioBroadcasterProvider implements BroadcasterProvider {

    private AudioRTMPBroadcaster mBroadcaster;

    @Override
    public Broadcaster getBroadcaster() {
        return mBroadcaster;
    }

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }

    @Override
    public void startBroadcaster(String broadcastURI, OnBroadcasterListener listener) {

    }

    @Override
    public void stopBroadcaster() {

    }
}
