/*
 LocalDevice
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.core;

/**
 * Local Device.
 * @author NTT DOCOMO, INC.
 */
public class LocalDevice {
    /** サービスID. */
    private String mServiceId;

    /** デバイス名称. */
    private String mDeviceName;

    /** 認可フラグ. */
    private Boolean mAuthenticationFlag;

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

    /**
     * 認可済みか判定.
     * @return true(認可中) / false(非認可).
     */
    public Boolean isAuthentication() {
        return mAuthenticationFlag;
    }

    /**
     * 認可フラグを設定する.
     * @param flag true(認可) / false(非認可).
     */
    public void setAuthenticationFlag(final Boolean flag) {
        mAuthenticationFlag = flag;
    }
}
