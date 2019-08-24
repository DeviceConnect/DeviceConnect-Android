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
public class ThetaDeviceManager implements ThetaDeviceDetection.DetectionListener,
                                                            WifiStateEventListener{

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    /**
     * An instance of {@link Context}.
     */
    private final Context mContext;
    /**
     * An THETA device which is currently connected.
     */
    private ThetaDevice mConnectedDevice;

    /**
     * A list of {@link ThetaDeviceEventListener}.
     */
    private final List<ThetaDeviceEventListener> mListeners = new ArrayList<ThetaDeviceEventListener>();

    /**
     * An instance of {@link ExecutorService}.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * THETA検知ロジックのリスト.
     */
    private final List<ThetaDeviceDetection> mDeviceDetections = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param context An instance of {@link Context}
     */
    public ThetaDeviceManager(final Context context) {
        mContext = context;
    }

    /**
     * THETA検知ロジックを追加する.
     *
     * @param detection THETA検知ロジック
     */
    public void addDeviceDetection(final ThetaDeviceDetection detection) {
        synchronized (mDeviceDetections) {
            for (ThetaDeviceDetection cache : mDeviceDetections) {
                if (cache == detection) {
                    return;
                }
            }
            mDeviceDetections.add(detection);
        }
    }

    /**
     * Gets the specified THETA device.
     *
     * @param id the identifier of THETA device
     * @return the specified THETA device
     */
    public ThetaDevice getConnectedDeviceById(final String id) {
        for (ThetaDevice device : getDetectedDevices()) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    private List<ThetaDevice> getDetectedDevices() {
        List<ThetaDevice> devices = new ArrayList<>();
        for (ThetaDeviceDetection detection : mDeviceDetections) {
            devices.addAll(detection.getDetectedDevices());
        }
        return devices;
    }

    /**
     * 検知されているデバイスのうち、任意のデバイスを1つ返却する.
     * 1つも検知されていない場合は <code>null</code> を返却する.
     *
     * @return {@link ThetaDevice} クラスのオブジェクト
     */
    public ThetaDevice getConnectedDevice() {
        List<ThetaDevice> devices = getDetectedDevices();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }
    /*
     * Check a THETA device which is connected currently.
     */
     public void checkConnectedDevice() {
         mExecutor.execute(() -> {
              WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
              WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
              if (wifiInfo != null) {
                  ThetaDevice device = ThetaDeviceFactory.createDeviceFromAccessPoint(mContext, wifiInfo);
                  if (device != null) {
                      mConnectedDevice = device;
                      notifyOnConnected(device);
                  }
              }
         });
      }

    /**
     * THETAデバイスの検知を開始する.
     */
    public void startDeviceDetection() {
        mExecutor.execute(() -> {
            for (ThetaDeviceDetection detection : mDeviceDetections) {
                detection.registerListener(ThetaDeviceManager.this);
                detection.start(mContext);
            }
        });
    }

    /**
     * THETAデバイスの検知を停止する.
     */
    public void stopDeviceDetection() {
        mExecutor.execute(() -> {
            for (ThetaDeviceDetection detection : mDeviceDetections) {
                detection.stop(mContext);
                detection.unregisterListener(ThetaDeviceManager.this);
            }
        });
    }

    /**
     * 後始末を実行する.
     */
    public void dispose() {
        stopDeviceDetection();
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
    public void onThetaDetected(final ThetaDevice device) {
        notifyOnConnected(device);
    }

    @Override
    public void onThetaLost(final ThetaDevice device) {
        notifyOnDisconnected(device);
    }

    @Override
    public void onNetworkChanged(WifiInfo wifiInfo) {
        mExecutor.execute(() -> {
            synchronized (this) {
                ThetaDevice oldDevice = mConnectedDevice;
                ThetaDevice newDevice = ThetaDeviceFactory.createDeviceFromAccessPoint(mContext, wifiInfo);
                mConnectedDevice = newDevice;

                if (oldDevice != null) {
                    notifyOnDisconnected(oldDevice);
                    oldDevice.destroy();
                }
                if (newDevice != null) {
                    notifyOnConnected(newDevice);
                }
            }
        });

    }

    @Override
    public void onWiFiEnabled() {

    }

    @Override
    public void onWiFiDisabled() {
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
