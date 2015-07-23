/*
 ThetaDeviceInfo
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.net.wifi.WifiInfo;

/**
 * Information of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceInfo {

    /**
     * An identifier as a device on Device Connect System.
     */
    public final String mServiceId;

    /**
     * A human-readable name.
     */
    public final String mName;

    /**
     * Constructor.
     *
     * @param wifiInfo an instance of {@link WifiInfo}
     */
    ThetaDeviceInfo(final WifiInfo wifiInfo) {
        mServiceId = "theta";
        mName = wifiInfo.getSSID().replace("\"", "");
    }
}
