package org.deviceconnect.android.deviceplugin.theta.core.wifi;

import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;

/**
 * WiFi State Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class WifiStateService extends Service {

    private WifiStateEventListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mListener = ((ThetaDeviceApplication) getApplication()).getDeviceManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                mListener.onNetworkChanged(wifiInfo);
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    mListener.onWiFiDisabled();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    mListener.onWiFiEnabled();
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

}
