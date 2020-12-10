package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster {
    void setOnBroadcasterEventListener(OnBroadcasterEventListener listener);
    void start();
    void stop();

    interface OnBroadcasterEventListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
