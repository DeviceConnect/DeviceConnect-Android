package org.deviceconnect.android.deviceplugin.theta.core;


import android.net.wifi.WifiInfo;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.core.wifi.WifiStateEventListener;

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
public class ThetaDeviceManager implements WifiStateEventListener {

//    /**
//     * An instance of {@link WifiManager}.
//     */
//    private final WifiManager mWifiMgr;

    /**
     * An THETA device which is currently connected.
     */
    private ThetaDevice mConnectedDevice;

//    /**
//     * Constructor.
//     *
//     * @param context an instance of {@link Context}
//     */
//    public ThetaDeviceManager(final Context context) {
//        mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//    }

    /**
     * Get a THETA device which is connected currently to the host device via WiFi.
     *
     * @return an instance of {@link ThetaDevice}
     */
    public ThetaDevice getConnectedDevice() {
        return mConnectedDevice;
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

    @Override
    public void onNetworkChanged(final WifiInfo wifiInfo) {
        mConnectedDevice = ThetaDeviceFactory.createDevice(wifiInfo);
        Log.d("AAA", "onNetworkChanged: " + mConnectedDevice);
    }

    @Override
    public void onWiFiEnabled() {
        // Nothig to do.
        Log.d("AAA", "onWiFiEnabled");
    }

    @Override
    public void onWiFiDisabled() {
        mConnectedDevice = null;
        Log.d("AAA", "onWiFiDisabled");
    }
}
