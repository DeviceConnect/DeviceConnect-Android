package org.deviceconnect.android.deviceplugin.theta.core;


/**
 * THETA Device Manager.
 *
 * <h2>Overview</h2>
 * <p>
 * THETA Device Manager manages a THETA devices which is connected to the Android host device.
 * </p>
 *
 * <h2>Usage</h2>
 * <code>
 *     // Obtain the Theta Device Manager.
 *     ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
 *     ThetaDeviceManager deviceMgr = app.getDeviceManager();
 *
 *     ThetaDevice device = deviceMgr.getConnectedDevice();
 *     if (device != null) {
 *       // Codes for case that a THETA device is connected.
 *     } else {
 *       // Codes for case that a THETA device is not connected.
 *     }
 * </code>
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
