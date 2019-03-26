/*
 NetworkUtil.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * ネットワークのアドレスを取得するためのユーティリティクラス.
 */
public final class NetworkUtil {
    /**
     * IPv4のローカルアドレス.
     */
    static final String LOCALHOST_IPV4 = "127.0.0.1";

    /**
     * IPv6のローカルアドレス.
     */
    static final String LOCALHOST_IPV6 = "::1";

    /**
     * IPv4が未設定.
     */
    static final String NOT_SET_IPV4 = "0.0.0.0";

    /**
     * IPv6が未設定.
     */
    static final String NOT_SET_IPV6 = "0:0:0:0:0:0:0:0";

    /**
     * IPv4のアドレスを保持します.
     */
    private static String mIPv4Address;

    /**
     * IPv4のアドレスを保持します.
     */
    private static String mIPv6Address;

    /**
     * 端末のIPアドレスがv6かを保持します.
     */
    private static boolean mIPv6Flag;

    /**
     * 端末のIPアドレスがv4かを保持します.
     */
    private static boolean mIPv4Flag;

    /**
     * WiFiのIPv4アドレスを保持します.
     */
    private static String mWifiIPv4Address;

    /**
     * WiFiのIPv6アドレスを保持します.
     */
    private static String mWifiIPv6Address;

    /**
     * WiFiのIPアドレスがv6かを保持します.
     */
    private static boolean mWifiIPv6Flag;

    /**
     * WiFiのIPアドレスがv4かを保持します.
     */
    private static boolean mWifiIPv4Flag;

    /**
     * モバイルネットワークのIPv4を保持します.
     */
    private static String mCellularIPv4Address;

    /**
     * モバイルネットワークのIPv6を保持します.
     */
    private static String mCellularIPv6Address;

    /**
     * モバイルネットワークのIPアドレスがv4かを保持します.
     */
    private static boolean mCellularIPv4Flag;

    /**
     * モバイルネットワークのIPアドレスがv6かを保持します.
     */
    private static boolean mCellularIPv6Flag;

    private NetworkUtil() {
    }

    /**
     * IPアドレスを取得します.
     */
    public static void getIpAddresses() {
        mIPv4Address = NOT_SET_IPV4;
        mIPv6Address = NOT_SET_IPV6;
        mIPv4Flag = false;
        mIPv6Flag = false;

        mWifiIPv4Address = NOT_SET_IPV4;
        mWifiIPv6Address = NOT_SET_IPV6;
        mWifiIPv4Flag = false;
        mWifiIPv6Flag = false;

        mCellularIPv4Address = NOT_SET_IPV4;
        mCellularIPv6Address = NOT_SET_IPV6;
        mCellularIPv4Flag = false;
        mCellularIPv6Flag = false;

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
                        setIPAddress(inetAddress, networkInterfaceName);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static void setIPAddress(final InetAddress inetAddress, final String interfaceName) {
        String ipAddress = inetAddress.getHostAddress();
        if (inetAddress instanceof Inet4Address) {
            if (interfaceName.contains("rmnet0") || interfaceName.contains("rmnet_data0")) {
                mCellularIPv4Address = ipAddress;
                mCellularIPv4Flag = true;
            } else if (interfaceName.contains("eth0")) {
                mIPv4Address = ipAddress;
                mIPv4Flag = true;
            } else if (interfaceName.contains("wlan0")) {
                mWifiIPv4Address = ipAddress;
                mWifiIPv4Flag = true;
            }
        } else if (inetAddress instanceof Inet6Address) {
            if (interfaceName.contains("rmnet0") || interfaceName.contains("rmnet_data0")) {
                mCellularIPv6Address = ipAddress.replace("%" + interfaceName, "");
                mCellularIPv6Flag = true;
            } else if (interfaceName.contains("eth0")) {
                mIPv6Address = ipAddress.replace("%" + interfaceName, "");
                mIPv6Flag = true;
            } else if (interfaceName.contains("wlan0")) {
                mWifiIPv6Address = ipAddress.replace("%" + interfaceName, "");
                mWifiIPv6Flag = true;
            }
        }
    }

    /**
     * 端末のIPv4アドレスを取得します.
     * @return IPv4アドレス
     */
    public static String getIPv4Address() {
        return mIPv4Address;
    }

    /**
     * 端末のIPv6アドレスを取得します.
     * @return IPv6アドレス
     */
    public static String getIPv6Address() {
        return mIPv6Address;
    }

    /**
     * 端末がIPv6か確認します.
     *
     * @return IPv6の場合はtrue、それ以外はfalse
     */
    public static boolean isIPv6() {
        return mIPv6Flag;
    }


    /**
     * 端末がIPv4か確認します.
     *
     * @return IPv4の場合はtrue、それ以外はfalse
     */
    public static boolean isIPv4() {
        return mIPv4Flag;
    }

    /**
     * WiFiのIPv4アドレスを取得します.
     * @return IPv4アドレス
     */
    public static String getWifiIPv4Address() {
        return mWifiIPv4Address;
    }

    /**
     * WiFiのIPv6アドレスを取得します.
     * @return IPv6アドレス
     */
    public static String getWifiIPv6Address() {
        return mWifiIPv6Address;
    }

    /**
     * 端末のWifiがIPv4か確認します.
     *
     * @return IPv4の場合はtrue、それ以外はfalse
     */
    public static boolean isWifiIPv4() {
        return mWifiIPv4Flag;
    }

    /**
     * 端末のWifiがIPv6か確認します.
     *
     * @return IPv6の場合はtrue、それ以外はfalse
     */
    public static boolean isWifiIPv6() {
        return mWifiIPv6Flag;
    }

    /**
     * モバイルネットワークのIPv4を取得します.
     * @return IPv4アドレス
     */
    public static String getCellularIPv4Address() {
        return mCellularIPv4Address;
    }

    /**
     * モバイルネットワークのIPv6を取得します.
     * @return IPv6アドレス
     */
    public static String getCellularIPv6Address() {
        return mCellularIPv6Address;
    }

    /**
     * モバイルネットワークがIPv4か確認します.
     *
     * @return IPv4の場合はtrue、それ以外はfalse
     */
    public static boolean isCellularIPv4Flag() {
        return mCellularIPv4Flag;
    }

    /**
     * モバイルネットワークがIPv6か確認します.
     *
     * @return IPv6の場合はtrue、それ以外はfalse
     */
    public static boolean isCellularIPv6Flag() {
        return mCellularIPv6Flag;
    }

    /**
     * WiFiに接続されているかを確認します.
     * @param context コンテキスト
     * @return WiFiに接続されている場合にはtrue、それ以外はfalse
     */
    public static boolean isWifiConnected(final Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo.isConnected();
    }

    /**
     * IPアドレスを取得します.
     * @return IPアドレス
     */
    public static String getIpAddress() {
        getIpAddresses();
        String ipAddress = NOT_SET_IPV4;
        if (NetworkUtil.isWifiIPv4()) {
            ipAddress = getWifiIPv4Address();
        } else if (NetworkUtil.isIPv4()) {
            ipAddress = getIPv4Address();
        } else if (NetworkUtil.isWifiIPv6()) {
            ipAddress = getWifiIPv6Address();
        } else if (NetworkUtil.isIPv6()) {
            ipAddress = getIPv6Address();
        }
        return ipAddress;
    }
}
