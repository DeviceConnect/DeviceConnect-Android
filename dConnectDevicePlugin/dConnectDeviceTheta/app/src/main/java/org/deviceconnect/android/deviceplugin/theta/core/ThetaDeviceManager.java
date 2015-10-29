package org.deviceconnect.android.deviceplugin.theta.core;


/**
 * THETA Device Manager.
 */
public class ThetaDeviceManager {

    /**
     * Get a THETA device which is connected currently to the host device via WiFi.
     *
     * @return an instance of {@link ThetaDevice}
     */
    public ThetaDevice getConnectedDevice() {
        // TODO Implement.
        return null;
    }

    /**
     * Register {@link ThetaDeviceEventListener}.
     * @param listener an instance of {@link ThetaDeviceEventListener}
     */
    public void registerDeviceEventListener(final ThetaDeviceEventListener listener) {
        // TODO Implement.
    }

    /**
     * Unregister {@link ThetaDeviceEventListener}.
     * @param listener an instance of {@link ThetaDeviceEventListener}
     */
    public void unregisterDeviceEventListener(final ThetaDeviceEventListener listener) {
        // TODO Implement.
    }

}
