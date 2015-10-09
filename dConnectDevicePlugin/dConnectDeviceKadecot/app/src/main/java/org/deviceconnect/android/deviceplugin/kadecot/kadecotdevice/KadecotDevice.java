/*
 KadecotDevice
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

/**
 * Kadecot Device.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotDevice {
    /** Device ID. */
    String mDeviceId = null;
    /** Protocol. */
    String mProtocol = null;
    /** Device Type. */
    String mDeviceType = null;
    /** Description. */
    String mDescription = null;
    /** Status. */
    String mStatus = null;
    /** Nickname. */
    String mNickname = null;
    /** IP Address. */
    String mIpAddr = null;
    /** Location. */
    String mLocation = null;
    /** Service ID. */
    String mServiceId = null;

    /** Constructor. */
    public KadecotDevice() {
    }

    /**
     * Get DeviceId.
     * @return DeviceId.
     */
    public String getDeviceId() {
        return mDeviceId;
    }

    /**
     * Get Protocol.
     * @return Protocol.
     */
    public String getProtocol() {
        return mProtocol;
    }

    /**
     * Get Device Type.
     * @return DeviceType.
     */
    public String getDeviceType() {
        return mDeviceType;
    }

    /**
     * Get Description.
     * @return Description.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get Status.
     * @return Status.
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * Get Nickname.
     * @return Nickname.
     */
    public String getNickname() {
        return mNickname;
    }

    /**
     * Get IP Address.
     * @return IP Address.
     */
    public String getIpAddr() {
        return mIpAddr;
    }

    /**
     * Get Location.
     * @return Location.
     */
    public String getLocation() {
        return mLocation;
    }

    /**
     * Get ServiceId.
     * @return ServiceId.
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * Set DeviceId.
     * @param deviceId DeviceId.
     */
    public void setDeviceId(final String deviceId) {
        mDeviceId = deviceId;
    }

    /**
     * Set Protocol.
     * @param protocol Protocol.
     */
    public void setProtocol(final String protocol) {
        mProtocol = protocol;
    }

    /**
     * Set Device Type.
     * @param deviceType Device Type.
     */
    public void setDeviceType(final String deviceType) {
        mDeviceType = deviceType;
    }

    /**
     * Set Description.
     * @param description Description.
     */
    public void setDescription(final String description) {
        mDescription = description;
    }

    /**
     * Set Status.
     * @param status Status.
     */
    public void setStatus(final String status) {
        mStatus = status;
    }

    /**
     * Set Nickname.
     * @param nickname Nickname.
     */
    public void setNickname(final String nickname) {
        mNickname = nickname;
    }

    /**
     *  Set IP Address.
     * @param ipAddr IP Address.
     */
    public void setIpAddr(final String ipAddr) {
        mIpAddr = ipAddr;
    }

    /**
     * Set Location.
     * @param location Location.
     */
    public void setLocation(final String location) {
        mLocation = location;
    }

    /**
     * Set ServiceId.
     * @param serviceId ServiceId.
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }
}
