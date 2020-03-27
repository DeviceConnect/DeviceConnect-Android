package org.deviceconnect.android.libmedia.streaming.util;

import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.deviceconnect.android.libmedia.BuildConfig;

/**
 * 端末に設定されている IP アドレスの一覧を取得するためのクラス.
 */
public final class IpAddressManager {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "IP";

    private InetAddress mIPv4Address;
    private InetAddress mIPv6Address;
    private InetAddress mWifiIPv4Address;
    private InetAddress mWifiIPv6Address;
    private InetAddress mBluetoothIPv4Address;
    private InetAddress mBluetoothIPv6Address;
    private InetAddress mVpnIPv4Address;
    private InetAddress mVpnIPv6Address;

    public IpAddressManager() {
        storeIPAddress();
    }

    /**
     * IP アドレスを取得して、各変数に格納します.
     */
    public void storeIPAddress() {
        Enumeration<NetworkInterface> enumeration;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface netIf = enumeration.nextElement();
                Enumeration<InetAddress> ipAddrs = netIf.getInetAddresses();
                while (ipAddrs.hasMoreElements()) {
                    InetAddress inetAddress = ipAddrs.nextElement();
                    if (!inetAddress.isLoopbackAddress() && netIf.isUp()) {
                        String networkInterfaceName = netIf.getName();
                        if (DEBUG) {
                            Log.d(TAG, networkInterfaceName + ": " + inetAddress);
                        }
                        setIPAddress(inetAddress, networkInterfaceName);
                    }
                }
            }
        } catch (SocketException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
    }

    private void setIPAddress(InetAddress inetAddress, String interfaceName) {
        if (inetAddress instanceof Inet4Address) {
            if (interfaceName.contains("rmnet")) {
                mIPv4Address = inetAddress;
            } else if (interfaceName.contains("wlan")) {
                mWifiIPv4Address = inetAddress;
            } else if (interfaceName.contains("bt-pan")) {
                mBluetoothIPv4Address = inetAddress;
            } else if (interfaceName.contains("ppp")) {
                mVpnIPv4Address = inetAddress;
            }
        } else if (inetAddress instanceof Inet6Address) {
            if (interfaceName.contains("rmnet")) {
                mIPv6Address = inetAddress;
            } else if (interfaceName.contains("wlan")) {
                mWifiIPv6Address = inetAddress;
            } else if (interfaceName.contains("bt-pan")) {
                mBluetoothIPv6Address = inetAddress;
            } else if (interfaceName.contains("ppp")) {
                mVpnIPv6Address = inetAddress;
            }
        }
    }

    public InetAddress getIPv4Address() {
        return mIPv4Address;
    }

    public InetAddress getIPv6Address() {
        return mIPv6Address;
    }

    public InetAddress getWifiIPv4Address() {
        return mWifiIPv4Address;
    }

    public InetAddress getWifiIPv6Address() {
        return mWifiIPv6Address;
    }

    public InetAddress getBluetoothIPv4Address() {
        return mBluetoothIPv4Address;
    }

    public InetAddress getBluetoothIPv6Address() {
        return mBluetoothIPv6Address;
    }

    public InetAddress getVpnIPv4Address() {
        return mVpnIPv4Address;
    }

    public InetAddress getVpnIPv6Address() {
        return mVpnIPv6Address;
    }
}
