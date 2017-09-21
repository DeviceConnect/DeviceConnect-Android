/*
 WiSunDevice.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.param;

import org.deviceconnect.android.deviceplugin.smartmeter.device.BP35C2;

/**
 * Wi-SUN USB Dongle の定義クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class WiSunDevice {
    /** 接続シリアルポート. */
    private String serialPort;
    /** ボーレート. */
    private int baudrate;
    /** デバイス種別. */
    private String deviceType;
    /** 接続先MACアドレス. */
    private String macAddress;
    /** 接続先IPv6アドレス. */
    private String ipv6Address;
    /** 接続周波数論理チャンネル. */
    private int channel;
    /** PAN ID. */
    private String panId;
    /** Bルート認証ID. */
    private String bRouteId;
    /** Bルート認証パスワード. */
    private String bRoutePassword;

    public WiSunDevice() {
        serialPort = "COM1";
        baudrate = BP35C2.Baudrate.BAUDRATE_115200.getBaudrate();
        deviceType = BP35C2.getDeviceType();
        macAddress = null;
        ipv6Address = null;
        channel = 0;
        panId = null;
        bRouteId = null;
        bRoutePassword = null;
    }

    /* Getter. */

    /**
     * シリアルポート取得.
     * @return シリアルポート.
     */
    public String getSerialPort() {
        return serialPort;
    }

    /**
     * ボーレート取得.
     * @return ボーレート.
     */
    public int getBaudrate() {
        return baudrate;
    }

    /**
     * デバイス種別取得
     * @return デバイス種別.
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * MACアドレス取得.
     * @return MACアドレス.
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * IPv6アドレス取得.
     * @return IPv6アドレス.
     */
    public String getIpv6Address() {
        return ipv6Address;
    }

    /**
     * チャンネル取得.
     * @return チャンネル.
     */
    public int getChannel() {
        return channel;
    }

    /**
     * PAN ID取得.
     * @return PAN ID.
     */
    public String getPanId() {
        return panId;
    }

    /**
     * Bルート認証ID取得.
     * @return Bルート認証ID.
     */
    public String getbRouteId() {
        return bRouteId;
    }

    /**
     * Bルート認証パスワード取得.
     * @return Bルート認証パスワード.
     */
    public String getbRoutePassword() {
        return bRoutePassword;
    }

    /* Setter. */

    /**
     * シリアルポート設定.
     * @param serialPort シリアルポート設定値.
     */
    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    /**
     * ボーレート設定.
     * @param baudrate ボーレート設定値.
     */
    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    /**
     * デバイス種別設定.
     * @param deviceType デバイス種別設定値.
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * MACアドレス設定.
     * @param macAddress MACアドレス設定値.
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * IPv6アドレス設定
     * @param ipv6Address IPv6アドレス設定値.
     */
    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    /**
     * チャンネル設定.
     * @param channel チャンネル設定値.
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }

    /**
     * PAN ID設定.
     * @param panId PAN ID設定値.
     */
    public void setPanId(String panId) {
        this.panId = panId;
    }

    /**
     * Bルート認証ID設定.
     * @param bRouteId Bルート認証ID設定値.
     */
    public void setbRouteId(String bRouteId) {
        this.bRouteId = bRouteId;
    }

    /**
     * Bルート認証パスワード設定.
     * @param bRoutePassword Bルート認証パスワード設定値.
     */
    public void setbRoutePassword(String bRoutePassword) {
        this.bRoutePassword = bRoutePassword;
    }
}
