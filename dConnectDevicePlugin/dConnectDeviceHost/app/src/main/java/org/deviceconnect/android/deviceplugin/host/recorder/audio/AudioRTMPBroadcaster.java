package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;

public class AudioRTMPBroadcaster implements Broadcaster {
    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public String getBroadcastURI() {
        return null;
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start(OnStartCallback callabck) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setMute(boolean mute) {

    }

    @Override
    public boolean isMute() {
        return false;
    }

    @Override
    public void onConfigChange() {

    }
}
