/*
 DeviceData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

import java.util.ArrayList;

/**
 * Device Data.
 *
 * @author NTT DOCOMO, INC.
 */
public class DeviceData {
    /** ECHONET Lite Class Name. */
    private String mENLClassName = "";
    /** DeviceConnect ServiceId. */
    private String mDCServiceId = "";
    /** DeviceConnect Scopes. */
    private ArrayList<String> mScopes;

    /**
     * Constructor.
     *
     * @param className ECHONET Lite Class.
     * @param serviceId DeviceConnect ServiceID.
     * @param scopes DeviceConnect Scope.
     */
    DeviceData(final String className, final String serviceId, final ArrayList<String> scopes) {
        this.mENLClassName = className;
        this.mDCServiceId = serviceId;
        this.mScopes = scopes;
    }

    /**
     * Get ECHONET Lite Class name.
     *
     * @return ECHONET Lite Class name.
     */
    String getENLClassName() {
        return mENLClassName;
    }

    /**
     * Get DeviceConnect ServiceId.
     *
     * @return DeviceConnect ServiceId.
     */
    String getDCServiceId() {
        return mDCServiceId;
    }

    /**
     * Get DeviceConnect Scopes.
     *
     * @return DeviceConnect ServiceId.
     */
    public ArrayList<String> getScopes() {
        return mScopes;
    }

    /**
     * Set ECHONET Lite Class name.
     *
     * @param className ECHONET Lite Class name.
     */
    void setENLClassName(final String className) {
        mENLClassName = className;
    }

    /**
     * Set DeviceConnect ServiceId.
     *
     * @param serviceId DeviceConnect ServiceId.
     */
    void setDCServiceId(final String serviceId) {
        mDCServiceId = serviceId;
    }

    /**
     * Set DeviceConnect Scopse.
     *
     * @param scopes DeviceConnect Scopes.
     */
    public void setScope(final ArrayList<String> scopes) {
        this.mScopes = scopes;
    }
}
