package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IVCNL4010 {

    void readProximity(final OnProximityListener listener);
    void startProximity(final OnProximityListener listener);
    void stopProximity(final OnProximityListener listener);

    void readAmbientLight();
    void startAmbientLight();
    void stopAmbientLight();

    interface OnProximityListener {
        void onStarted();
        void onData(final double proximity);
        void onError(final String message);
    }
}
