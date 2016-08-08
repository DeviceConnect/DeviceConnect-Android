package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.deviceconnect.android.deviceplugin.theta.core.wifi.WifiStateEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

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
     * An instance of {@link ExecutorService}.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * Constructor.
     *
     * @param context An instance of {@link Context}
     */
    public ThetaDeviceManager(final Context context) {
        mContext = context;
    }

    /**
     * Gets the specified THETA device.
     *
     * @param id the identifier of THETA device
     * @return the specified THETA device
     */
    public ThetaDevice getConnectedDeviceById(final String id) {
        ThetaDevice device = mConnectedDevice;
        if (device == null) {
            return null;
        }
        if (device.getId().equals(id)) {
            return device;
        } else {
            return null;
        }
    }

    /**
     * Get a THETA device which is connected currently to the host device via WiFi.
     *
     * @return an instance of {@link ThetaDevice}
     */
    public ThetaDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    /**
     * Check a THETA device which is connected currently.
     */
    public void checkConnectedDevice() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if (wifiInfo != null) {
                    ThetaDevice device = ThetaDeviceFactory.createDevice(mContext, wifiInfo);
                    if (device != null) {
                        mConnectedDevice = device;
                        notifyOnConnected(device);
                    }
                }
            }
        });
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    ThetaDevice oldDevice = mConnectedDevice;
                    ThetaDevice newDevice = ThetaDeviceFactory.createDevice(mContext, wifiInfo);
                    mConnectedDevice = newDevice;
                    mLogger.info("onNetworkChanged: " + mConnectedDevice);

                    if (oldDevice != null) {
                        notifyOnDisconnected(oldDevice);
                        oldDevice.destroy();
                    }
                    if (newDevice != null) {
                        notifyOnConnected(newDevice);
                    }
                }
            }
        });
    }

    @Override
    public void onWiFiEnabled() {
        // Nothing to do.
        mLogger.info("onWiFiEnabled");
    }

    @Override
    public void onWiFiDisabled() {
        mLogger.info("onWiFiDisabled");
        synchronized (this) {
            ThetaDevice oldDevice = mConnectedDevice;
            mConnectedDevice = null;
            if (oldDevice != null) {
                notifyOnDisconnected(oldDevice);
                oldDevice.destroy();
            }
        }
    }
}
