package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.core.wifi.WifiStateEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    /**
     * An THETA device which is currently connected.
     */
    private ThetaDevice mConnectedDevice;

    /**
     * An instance of {@link Context}.
     */
    private final Context mContext;

    /**
     * A list of {@link ThetaDeviceEventListener}.
     */
    private final List<ThetaDeviceEventListener> mListeners = new ArrayList<ThetaDeviceEventListener>();

    /**
     * Constructor.
     *
     * @param context An instance of {@link Context}
     */
    public ThetaDeviceManager(final Context context) {
        mContext = context;
    }

    /**
     * Get a THETA device which is connected currently to the host device via WiFi.
     *
     * @return an instance of {@link ThetaDevice}
     */
    public ThetaDevice getConnectedDevice() {
        if (mConnectedDevice == null) {
            WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null) {
                mConnectedDevice = ThetaDeviceFactory.createDevice(mContext, wifiInfo);
            }
        }
        return mConnectedDevice;
    }

    /**
     * Register {@link ThetaDeviceEventListener}.
     * @param listener an instance of {@link ThetaDeviceEventListener}
     */
    public void registerDeviceEventListener(final ThetaDeviceEventListener listener) {
        synchronized (mListeners) {
            for (Iterator<ThetaDeviceEventListener> it = mListeners.iterator(); it.hasNext(); ) {
                ThetaDeviceEventListener l = it.next();
                if (l == listener) {
                    return;
                }
            }
            mListeners.add(listener);
        }
    }

    /**
     * Unregister {@link ThetaDeviceEventListener}.
     * @param listener an instance of {@link ThetaDeviceEventListener}
     */
    public void unregisterDeviceEventListener(final ThetaDeviceEventListener listener) {
        synchronized (mListeners) {
            for (Iterator<ThetaDeviceEventListener> it = mListeners.iterator(); it.hasNext(); ) {
                ThetaDeviceEventListener l = it.next();
                if (l == listener) {
                    it.remove();
                    break;
                }
            }
        }
    }

    private void notifyOnConnected(final ThetaDevice device) {
        synchronized (mListeners) {
            for (ThetaDeviceEventListener l : mListeners) {
                l.onConnected(device);
            }
        }
    }

    private void notifyOnDisconnected(final ThetaDevice device) {
        synchronized (mListeners) {
            for (ThetaDeviceEventListener l : mListeners) {
                l.onDisconnected(device);
            }
        }
    }

    @Override
    public void onNetworkChanged(final WifiInfo wifiInfo) {
        synchronized (this) {
            ThetaDevice oldDevice = mConnectedDevice;
            ThetaDevice newDevice = ThetaDeviceFactory.createDevice(mContext, wifiInfo);
            mConnectedDevice = newDevice;

            if (oldDevice != null) {
                notifyOnDisconnected(oldDevice);
            }
            if (newDevice != null) {
                notifyOnConnected(newDevice);
            }
        }
        Log.d("AAA", "onNetworkChanged: " + mConnectedDevice);
    }

    @Override
    public void onWiFiEnabled() {
        // Nothig to do.
        Log.d("AAA", "onWiFiEnabled");
    }

    @Override
    public void onWiFiDisabled() {
        synchronized (this) {
            ThetaDevice oldDevice = mConnectedDevice;
            mConnectedDevice = null;
            if (oldDevice != null) {
                notifyOnDisconnected(oldDevice);
            }
        }
        Log.d("AAA", "onWiFiDisabled");
    }
}
