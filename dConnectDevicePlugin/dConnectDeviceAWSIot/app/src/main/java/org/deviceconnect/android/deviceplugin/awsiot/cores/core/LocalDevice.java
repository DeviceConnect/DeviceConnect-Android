/*
 LocalDevice.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

/**
 * Local Device.
 * @author NTT DOCOMO, INC.
 */
public class LocalDevice {
    /** サービスID. */
    private String mServiceId;
    /** デバイス名称. */
    private String mDeviceName;

    /**
     * コンストラクター.
     * @param serviceId サービスID.
     * @param deviceName デバイス名称.
     */
    public LocalDevice(final String serviceId, final String deviceName) {
        mServiceId = serviceId;
        mDeviceName = deviceName;
    }

    /**
     * サービスIDを取得する.
     * @return サービスID.
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * サービスIDを設定する.
     * @param id サービスID.
     */
    public void setServiceId(final String id) {
        mServiceId = id;
    }

    /**
     * デバイス名称を取得する.
     * @return 名称.
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * デバイス名称を設定する.
     * @param name 名称.
     */
    public void setDeviceName(final String name) {
        mDeviceName = name;
    }
}
