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
                return new ThetaM15(context, ssId);
            case THETA_S:
                return new ThetaS(ssId);
            default:
                return null;
        }
    }

    private static String parseSSID(final WifiInfo wifiInfo) {
        String ssId = wifiInfo.getSSID();
        if (ssId == null) {
            return null;
        }
        return ssId.replace("\"", "");
    }

    private static ThetaDeviceModel parseModel(final String ssId) {
        if (ssId.startsWith("THETAXN")) {
            return ThetaDeviceModel.THETA_M15;
        } else if (ssId.startsWith("THETAXS")) {
            return ThetaDeviceModel.THETA_S;
        } else {
            return ThetaDeviceModel.UNKNOWN;
        }
    }

}
