package org.deviceconnect.android.deviceplugin.theta.core;

import android.content.Context;
import android.net.wifi.WifiInfo;

class ThetaDeviceFactory {

    private ThetaDeviceFactory() {
    }

    public static ThetaDevice createDevice(final Context context, final WifiInfo wifiInfo) {
        String ssId = parseSSID(wifiInfo);
        if (ssId == null) {
            return null;
        }
        ThetaDeviceModel model = parseModel(ssId);
        switch (model) {
            case THETA_M15:
                ThetaM15 m15 = new ThetaM15(context, ssId);
                if (m15.initialize()) {
                    return m15;
                } else {
                    return null;
                }
            case THETA_S:
                return new ThetaS(ssId);
            case THETA_V:
                return new ThetaV(ssId);
            default:
                return null;
        }
    }

    private static String parseSSID(final WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return null;
        }
        String ssId = wifiInfo.getSSID();
        if (ssId == null) {
            return null;
        }
        return ssId.replace("\"", "");
    }

    private static ThetaDeviceModel parseModel(final String ssId) {
        if (ssId.startsWith("THETAXN")) {
            return ThetaDeviceModel.THETA_M15;
        } else if (ssId.startsWith("THETAXS") || ssId.startsWith("THETAYJ")) {
            return ThetaDeviceModel.THETA_S;
        } else if ( ssId.startsWith("THETAYL")) {
            return ThetaDeviceModel.THETA_V;
        } else {
            return ThetaDeviceModel.UNKNOWN;
        }
    }

}
