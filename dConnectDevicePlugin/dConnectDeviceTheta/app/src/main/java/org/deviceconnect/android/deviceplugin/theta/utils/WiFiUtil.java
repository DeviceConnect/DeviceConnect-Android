/*
WiFiUtil
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.theta.utils;

/**
 * WiFi Utility.
 * @author NTT DOCOMO, INC.
 */
public final class WiFiUtil {
    /** WiFi prefix of Theta. */
    private static final String WIFI_PREFIX = "THETA";

    /**
     * Constructor.
     */
    private WiFiUtil() {
    }

    /**
     * Specified SSID to check whether the SSID of Wifi of SonyCamera device.
     * 
     * @param ssid SSID
     * @return True if the SSID of Theta device, otherwise false
     */
    public static boolean checkSSID(final String ssid) {
        if (ssid == null) {
            return false;
        }
        String id = ssid.replace("\"", "");
        return id.startsWith(WIFI_PREFIX);
    }
}
