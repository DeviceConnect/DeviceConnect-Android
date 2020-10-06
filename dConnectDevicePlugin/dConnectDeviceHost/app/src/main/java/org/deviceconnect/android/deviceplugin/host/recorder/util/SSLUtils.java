package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Locale;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * httpsのMJPEGサーバを起動する時に必要になってくるUtility.
 */
public class SSLUtils {
    /**
     * DeviceConnectのデフォルトのパスワード.
     */
    public static final String DEFAULT_SSL_PASSWORD = "0000";
    /**
     * Gets the ip address.
     *
     * @param context Context of application
     * @return Returns ip address
     */
    public static String getIPAddress(final Context context) {
        Context appContext = context.getApplicationContext();
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cManager.getActiveNetworkInfo();
        String en0Ip = null;
        if (network != null) {
            switch (network.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (inetAddress instanceof Inet4Address
                                        && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                    en0Ip = inetAddress.getHostAddress();
                                    break;
                                }
                            }
                        }
                    } catch (SocketException e) {
                        Log.e("Host", "Get Ethernet IP Error", e);
                    }
            }
        }

        if (en0Ip != null) {
            return en0Ip;
        } else {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
    }
}
