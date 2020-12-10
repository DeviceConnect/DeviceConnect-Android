package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster {
    String getMimeType();
    String getBroadcastURI();

    void setOnBroadcasterEventListener(OnBroadcasterEventListener listener);

    boolean isRunning();
    void start();
    void stop();

    interface OnBroadcasterEventListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
