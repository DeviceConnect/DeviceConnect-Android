package org.deviceconnect.android.deviceplugin.theta.core;

/**
 * THETA Device Event
 */
public interface ThetaDeviceEventListener {

    /**
     * Notify that a THETA device has been connected newly.
     *
     * @param device an instance of {@link ThetaDevice}
     */
    void onConnected(final ThetaDevice device);

    /**
     * Notify that a THETA device has been disconnected.
     *
     * @param device an instance of {@link ThetaDevice}
     */
    void onDisconnected(final ThetaDevice device);

}
