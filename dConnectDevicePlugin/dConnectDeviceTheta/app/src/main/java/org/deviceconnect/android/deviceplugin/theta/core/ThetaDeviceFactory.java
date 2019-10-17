package org.deviceconnect.android.deviceplugin.theta.core;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.burgstaller.okhttp.digest.Credentials;

import java.net.InetAddress;

import javax.net.SocketFactory;

public class ThetaDeviceFactory {

    private static final String DEFAULT_HOST = "192.168.1.1";

    private ThetaDeviceFactory() {
    }

    public static ThetaDevice createDeviceFromAccessPoint(final Context context, final WifiInfo wifiInfo) {
        return createDeviceFromAccessPoint(context, parseSSID(wifiInfo), null);
    }

    public static ThetaDevice createDeviceFromAccessPoint(final Context context, final String ssId, final SocketFactory socketFactory) {
        String name = ssId;
        if (name == null) {
            WifiManager wm = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wm == null) {
                return null;
            }
            WifiInfo info = wm.getConnectionInfo();
            if (info == null) {
                return null;
            }
            name = info.getSSID().replace("\"", "");
        }
        ThetaDeviceModel model = parseModel(name);
        switch (model) {
            case THETA_M15:
                ThetaM15 m15 = new ThetaM15(context, name);
                if (m15.initialize()) {
                    return m15;
                } else {
                    return null;
                }
            case THETA_S:
                return new ThetaS(name, DEFAULT_HOST, socketFactory);
            case THETA_V:
                return new ThetaV(name, DEFAULT_HOST, null, socketFactory);
            default:
                return null;
        }
    }

    public static ThetaDevice createDeviceFromNsdServiceInfo(final NsdServiceInfo serviceInfo) {
        InetAddress host = serviceInfo.getHost();
        if (host == null) {
            return null;
        }
        String serviceName = serviceInfo.getServiceName();
        if (serviceName == null) {
            return null;
        }
        // サービス名がSSIDと同一である前提
        ThetaDeviceModel model = parseModel(serviceName);
        switch (model) {
            case THETA_V:
                String password = parsePasswordForDigestAuthentication(serviceName);
                Credentials credentials = new Credentials(serviceName, password);
                return new ThetaV(serviceName, host.getHostAddress(), credentials, null);
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
        } else if (ssId.startsWith("THETAYL") || ssId.startsWith("THETAYN")) {
            return ThetaDeviceModel.THETA_V;
        } else {
            return ThetaDeviceModel.UNKNOWN;
        }
    }

    private static String parsePasswordForDigestAuthentication(final String serviceName) {
        if (serviceName.startsWith("THETAYL")) {
            return serviceName.substring("THETAYL".length());
        } else {
            return null;
        }
    }
}
