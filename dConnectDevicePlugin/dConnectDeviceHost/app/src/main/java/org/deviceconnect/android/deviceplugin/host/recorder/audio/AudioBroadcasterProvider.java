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
    public Broadcaster startBroadcaster(String broadcastURI) {
        return mBroadcaster;
    }

    @Override
    public void stopBroadcaster() {
        if (mBroadcaster != null) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }

    @Override
    public void onConfigChange() {
        if (mBroadcaster != null) {
            mBroadcaster.onConfigChange();
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mBroadcaster != null) {
            mBroadcaster.setMute(mute);
        }
    }
}
