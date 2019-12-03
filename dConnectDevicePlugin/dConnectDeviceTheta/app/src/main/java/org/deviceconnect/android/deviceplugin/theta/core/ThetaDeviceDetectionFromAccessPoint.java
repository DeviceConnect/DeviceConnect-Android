package org.deviceconnect.android.deviceplugin.theta.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ThetaDeviceDetectionFromAccessPoint extends AbstractThetaDeviceDetection {

    private Context mContext;

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    private ThetaDevice mConnectedDevice;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsStarted;

    private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if (wifiInfo != null) {
                    onNetworkChanged(wifiInfo);
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        onWiFiDisabled();
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        onWiFiEnabled();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    public synchronized void start(final Context context) {
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;
        mContext = context;

        // 現在の接続状況を確認
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null) {
                ThetaDevice device = ThetaDeviceFactory.createDeviceFromAccessPoint(mContext, wifiInfo);
                if (device != null) {
                    mConnectedDevice = device;
                    notifyOnThetaDetected(device);
                }
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public synchronized void stop(final Context context) {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;
        context.unregisterReceiver(mWifiReceiver);
        mContext = null;
    }

    @Override
    public List<ThetaDevice> getDetectedDevices() {
        List<ThetaDevice> devices = new ArrayList<>();
        ThetaDevice cache = mConnectedDevice;
        if (cache != null) {
            devices.add(cache);
        }
        return devices;
    }

    private void onNetworkChanged(final WifiInfo wifiInfo) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    mLogger.info("onNetworkChanged: state=" + wifiInfo.getSupplicantState());
                    if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                        return;
                    }
                    mLogger.info("onNetworkChanged: SSID=" + wifiInfo.getSSID());

                    ThetaDevice oldDevice = mConnectedDevice;
                    if (oldDevice != null && oldDevice.getName().equals(wifiInfo.getSSID())) {
                        mLogger.info("onNetworkChanged: Connected already: SSID=" + wifiInfo.getSSID());
                        return;
                    }


                    ThetaDevice newDevice = ThetaDeviceFactory.createDeviceFromAccessPoint(mContext, wifiInfo);
                    mLogger.info("onNetworkChanged: THETA Device: " + newDevice);

                    if (isLostOldDevice(oldDevice, newDevice)) {
                        mLogger.info("onNetworkChanged: isLostOldDevice: " + oldDevice.getId());
                        notifyOnThetaLost(oldDevice);
                        oldDevice.destroy();
                    }
                    if (isFoundNewDevice(oldDevice, newDevice)) {
                        mLogger.info("onNetworkChanged: isFoundNewDevice: " + newDevice.getId());
                        notifyOnThetaDetected(newDevice);
                    }
                    mConnectedDevice = newDevice;
                }
            }
        });
    }

    private boolean isLostOldDevice(final ThetaDevice oldDevice, final ThetaDevice newDevice) {
        if (oldDevice == null) {
            return false;
        } else {
            if (newDevice == null) {
                return true;
            } else {
                return isSameDevice(oldDevice, newDevice);
            }
        }
    }

    private boolean isFoundNewDevice(final ThetaDevice oldDevice, final ThetaDevice newDevice) {
        if (newDevice == null) {
            return false;
        } else {
            if (oldDevice == null) {
                return true;
            } else {
                return isSameDevice(oldDevice, newDevice);
            }
        }
    }

    private boolean isSameDevice(final @NonNull ThetaDevice a, final @NonNull ThetaDevice b) {
        return a.getId().equals(b.getId());
    }

    private void onWiFiEnabled() {
        // Nothing to do.
        mLogger.info("onWiFiEnabled");
    }

    private void onWiFiDisabled() {
        mLogger.info("onWiFiDisabled");
        synchronized (this) {
            ThetaDevice oldDevice = mConnectedDevice;
            mConnectedDevice = null;
            if (oldDevice != null) {
                notifyOnThetaLost(oldDevice);
                oldDevice.destroy();
            }
        }
    }
}
