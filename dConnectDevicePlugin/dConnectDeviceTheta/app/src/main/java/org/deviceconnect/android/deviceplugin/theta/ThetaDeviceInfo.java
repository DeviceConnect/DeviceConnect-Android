package org.deviceconnect.android.deviceplugin.theta;

import android.net.wifi.WifiInfo;

public class ThetaDeviceInfo {
    public final String mServiceId;
    public final String mName;

    ThetaDeviceInfo(final WifiInfo wifiInfo) {
        mServiceId = "theta";
        mName = wifiInfo.getSSID().replace("\"", "");
    }
}
