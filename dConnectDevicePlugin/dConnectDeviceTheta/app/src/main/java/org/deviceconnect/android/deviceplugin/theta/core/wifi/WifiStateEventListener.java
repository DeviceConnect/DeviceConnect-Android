package org.deviceconnect.android.deviceplugin.theta.core.wifi;


import android.net.wifi.WifiInfo;

public interface WifiStateEventListener {

    void onNetworkChanged(WifiInfo wifiInfo);

    void onWiFiEnabled();

    void onWiFiDisabled();

}
