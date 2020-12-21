package org.deviceconnect.android.deviceplugin.host.sensor;

import java.util.List;

public interface HostKeyEventObserver {
    void observeKeyEvent(List<HostKeyEvent> event);

    class HostKeyEvent {
        private int mState;
        private int mConfig;
        private int mId;
    }
}
