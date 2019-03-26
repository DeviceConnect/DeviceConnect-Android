/*
 IpAddressFetcher.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 端末の IP アドレスを取得するためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class IpAddressFetcher {
    /**
     * 有線 LAN に接続されていない場合の IP v4 アドレスを定義.
     */
    public static final String IP_V4_ADDRESS = "0.0.0.0";

    /**
     * 有線 LAN に接続されていない場合の IP v6 アドレスを定義.
     */
    public static final String IP_V6_ADDRESS = "0:0:0:0:0:0:0:0";

    /**
     * Wi-Fi に接続されていない場合の IP v4 アドレスを定義.
     */
    public static final String WIFI_IP_V4_ADDRESS = "0.0.0.0";

    /**
     * Wi-Fi に接続されていない場合の IP v6 アドレスを定義.
     */
    public static final String WIFI_IP_V6_ADDRESS = "0:0:0:0:0:0:0:0";

    /**
     * Bluetooth に接続されていない場合の IP v4 アドレスを定義.
     */
    public static final String BT_IP_V4_ADDRESS = "0.0.0.0";

    /**
     * Bluetooth に接続されていない場合の IP v6 アドレスを定義.
     */
    public static final String BT_IP_V6_ADDRESS = "0:0:0:0:0:0:0:0";

    /**
     * VPN に接続されていない場合の IP v4 アドレスを定義.
     */
    public static final String VPN_IP_V4_ADDRESS = "0.0.0.0";

    /**
     * VPN に接続されていない場合の IP v6 アドレスを定義.
     */
    public static final String VPN_IP_V6_ADDRESS = "0:0:0:0:0:0:0:0";

    /**
     * 有線 LAN に接続されている IP v4 のアドレス.
     */
    private String mIPv4Address = IP_V4_ADDRESS;

    /**
     * 有線 LAN に接続されている IP v6 のアドレス.
     */
    private String mIPv6Address = IP_V6_ADDRESS;

    /**
     * Wi-Fi に接続されている IP v4 のアドレス.
     */
    private String mWifiIPv4Address = WIFI_IP_V4_ADDRESS;

    /**
     * Wi-Fi に接続されている IP v6 のアドレス.
     */
    private String mWifiIPv6Address = WIFI_IP_V6_ADDRESS;

    /**
     * Bluetooth 経由でネットワークに接続されている IP v4 のアドレス.
     */
    private String mBluetoothIPv4Address = BT_IP_V4_ADDRESS;

    /**
     * Bluetooth 経由でネットワークに接続されている IP v6 のアドレス.
     */
    private String mBluetoothIPv6Address = BT_IP_V6_ADDRESS;

    /**
     * VPN 経由でネットワークに接続されている IP v4 のアドレス.
     */
    private String mVpnIPv4Address = VPN_IP_V4_ADDRESS;

    /**
     * VPN 経由でネットワークに接続されている IP v6 のアドレス.
     */
    private String mVpnIPv6Address = VPN_IP_V6_ADDRESS;

    /**
     * コンストラクタ.
     */
    public IpAddressFetcher() {
        updateIpAddressInfo();
    }

    /**
     * IPアドレス情報を更新します.
     */
    public void updateIpAddressInfo() {
        mIPv4Address = IP_V4_ADDRESS;
        mIPv6Address = IP_V6_ADDRESS;
        mWifiIPv4Address = WIFI_IP_V4_ADDRESS;
        mWifiIPv6Address = WIFI_IP_V6_ADDRESS;
        mBluetoothIPv4Address = BT_IP_V4_ADDRESS;
        mBluetoothIPv6Address = BT_IP_V6_ADDRESS;
        mVpnIPv4Address = VPN_IP_V4_ADDRESS;
        mVpnIPv6Address = VPN_IP_V6_ADDRESS;

        Enumeration<NetworkInterface> enumeration;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface netIf = enumeration.nextElement();
                Enumeration<InetAddress> ipAddrs = netIf.getInetAddresses();
                while (ipAddrs.hasMoreElements()) {
                    InetAddress inetAddress = ipAddrs.nextElement();
                    if (!inetAddress.isLoopbackAddress() && netIf.isUp()) {
                        setIPAddress(inetAddress, netIf.getName());
                    }
                }
            }
        } catch (SocketException e) {
            // ignore.
        }
    }

    /**
     * 指定されたインターフェース名のIPアドレスを設定します.
     *
     * @param inetAddress IPアドレス情報
     * @param interfaceName インターフェース名
     */
    private void setIPAddress(final InetAddress inetAddress, final String interfaceName) {
        String ipAddress = inetAddress.getHostAddress();
        if (inetAddress instanceof Inet4Address) {
            if (interfaceName.contains("rmnet")) {
                mIPv4Address = ipAddress;
            } else if (interfaceName.contains("wlan")) {
                mWifiIPv4Address = ipAddress;
            } else if (interfaceName.contains("bt-pan")) {
                mBluetoothIPv4Address = ipAddress;
            } else if (interfaceName.contains("ppp")) {
                mVpnIPv4Address = ipAddress;
            }

        } else if (inetAddress instanceof Inet6Address) {
            if (interfaceName.contains("rmnet")) {
                mIPv6Address = ipAddress.replace("%" + interfaceName, "");
            } else if (interfaceName.contains("wlan")) {
                mWifiIPv6Address = ipAddress.replace("%" + interfaceName, "");
            } else if (interfaceName.contains("bt-pan")) {
                mBluetoothIPv6Address = ipAddress.replace("%" + interfaceName, "");
            } else if (interfaceName.contains("ppp")) {
                mVpnIPv6Address = ipAddress.replace("%" + interfaceName, "");
            }
        }
    }

    /**
     * 有線 LAN に接続されている IP v4 のアドレスを取得します.
     *
     * @return IP v4 のアドレス
     */
    public String getIPv4Address() {
        return mIPv4Address;
    }

    /**
     * 有線 LAN に接続されている IP v4 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasIPv4Address() {
        return !IP_V4_ADDRESS.equals(getIPv4Address());
    }

    /**
     * 有線 LAN に接続されている IP v6 のアドレスを取得します.
     *
     * @return IP v6 のアドレス
     */
    public String getIPv6Address() {
        return mIPv6Address;
    }

    /**
     * 有線 LAN に接続されている IP v6 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasIPv6Address() {
        return !IP_V6_ADDRESS.equals(getIPv6Address());
    }

    /**
     * Wi-Fi に接続されている IP v4 のアドレスを取得します.
     *
     * @return IP v4 のアドレス
     */
    public String getWifiIPv4Address() {
        return mWifiIPv4Address;
    }

    /**
     * Wi-Fi に接続されている IP v4 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasWifiIPv4Address() {
        return !WIFI_IP_V4_ADDRESS.equals(getWifiIPv4Address());
    }

    /**
     * Wi-Fi に接続されている IP v6 のアドレスを取得します.
     *
     * @return IP v6 のアドレス
     */
    public String getWifiIPv6Address() {
        return mWifiIPv6Address;
    }

    /**
     * Wi-Fi に接続されている IP v6 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasWifiIPv6Address() {
        return !WIFI_IP_V6_ADDRESS.equals(getWifiIPv6Address());
    }

    /**
     * Bluetooth 経由でネットワークに接続されている IP v4 のアドレスを取得します.
     *
     * @return IP v4 のアドレス
     */
    public String getBtIPv4Address() {
        return mBluetoothIPv4Address;
    }

    /**
     * Bluetooth 経由でネットワークに接続されている IP v4 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasBtIPv4Address() {
        return !BT_IP_V4_ADDRESS.equals(getBtIPv4Address());
    }

    /**
     * Bluetooth 経由でネットワークに接続されている IP v6 のアドレスを取得します.
     *
     * @return IP v6 のアドレス
     */
    public String getBtIPv6Address() {
        return mBluetoothIPv6Address;
    }

    /**
     * Bluetooth 経由でネットワークに接続されている IP v6 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasBtIPv6Address() {
        return !BT_IP_V6_ADDRESS.equals(getBtIPv6Address());
    }

    /**
     * VPN 経由でネットワークに接続されている IP v4 のアドレスを取得します.
     *
     * @return IP v4 のアドレス
     */
    public String getVpnIPv4Address() {
        return mVpnIPv4Address;
    }

    /**
     * VPN 経由でネットワークに接続されている IP v4 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasVpnIPv4Address() {
        return !VPN_IP_V4_ADDRESS.equals(getVpnIPv4Address());
    }

    /**
     * VPN 経由でネットワークに接続されている IP v6 のアドレスを取得します.
     *
     * @return IP v6 のアドレス
     */
    public String getVpnIPv6Address() {
        return mVpnIPv6Address;
    }

    /**
     * VPN 経由でネットワークに接続されている IP v6 のアドレスを持っているか確認します.
     *
     * @return 持っている場合はtrue、それ以外はfalse
     */
    public boolean hasVpnIPv6Address() {
        return !VPN_IP_V6_ADDRESS.equals(getVpnIPv6Address());
    }
}
