/*
 KadecotResult
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

/**
 * Kadecot Result Class.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotResult {
    /** Kadecot Server Result. */
    String mServerResult = null;
    /** Property Name. */
    String mPropertyName = null;
    /** Property Value. */
    String mPropertyValue = null;

    /** Constructor. */
    public KadecotResult() {
    }

    /**
     * Get server result.
     *
     * @return Server result.
     */
    public String getServerResult() {
        return mServerResult;
    }

    /**
     * Get property name.
     *
     * @return Property name.
     */
    public String getPropertyName() {
        return mPropertyName;
    }

    /**
     * Get property value.
     *
     * @return Property value.
     */
    public String getPropertyValue() {
        return mPropertyValue;
    }

    /**
     * Set server result.
     *
     * @param result Server result.
     */
    public void setServerResult(final String result) {
        mServerResult = result;
    }

    /**
     * Set property name.
     *
     * @param name Property name.
     */
    public void setPropertyName(final String name) {
        mPropertyName = name;
    }

    /**
     * Set property value.
     *
     * @param value Property value.
     */
    public void setPropertyValue(final String value) {
        mPropertyValue = value;
    }
}
