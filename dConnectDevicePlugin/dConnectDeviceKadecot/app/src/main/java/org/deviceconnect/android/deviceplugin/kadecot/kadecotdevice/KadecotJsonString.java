/*
 KadecotJsonString
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

/**
 * KadecotJsonString.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotJsonString {
    /** Index. */
    int mIndex = -1;
    /** Property Name. */
    String mPropertyName = null;
    /** Procedure. */
    String mProcedure = null;
    /** Default Value. */
    int mDefaultValue = -1;
    /** Base URI. */
    String mBaseUrl = "content://com.sonycsl.kadecot.json.provider/jsonp/v1/devices/";
    /** Procedure strings. */
    String mProcedureStr = "?procedure=";
    /** Parameter strings. */
    String mParamStr = "&params={\"propertyName\":\"";
    /** Value strings. */
    String mValueStr = "\",\"propertyValue\":[";
    /** Get procedure end strings. */
    String mGetEndStr = "\"}";
    /** Set procedure end string. */
    String mSetEndStr = "]}";

    /**
     * Constructor.
     * @param index Index.
     * @param procedure Procedure. (get/set)
     * @param propertyName Property Name.
     */
    KadecotJsonString(final int index, final String procedure, final String propertyName) {
        this.mIndex = index;
        this.mProcedure = procedure;
        this.mPropertyName = propertyName;
        this.mDefaultValue = -1;
    }

    /**
     * Constructor.
     * @param index Index.
     * @param procedure Procedure.(get/set)
     * @param propertyName Property Name.
     * @param defaultValue Default Value.
     */
    KadecotJsonString(final int index, final String procedure, final String propertyName, final int defaultValue) {
        this.mIndex = index;
        this.mProcedure = procedure;
        this.mPropertyName = propertyName;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Get Index.
      * @return Index.
     */
    int getIndex() {
        return mIndex;
    }

    /**
     * Get Procedure.
     * @return Procedure.
     */
    String getProcedure() {
        return mProcedure;
    }

    /**
     * Get Property name.
     * @return Property name.
     */
    String getPropertyName() {
        return mPropertyName;
    }

    /**
     * Get Default value.
     * @return Default value.
     */
    int getDefautValue() {
        return mDefaultValue;
    }

    /**
     * Get JSON string.
     * @param deviceId DeviceId.
     * @return JSON string.
     */
    String getJsonString(final String deviceId) {
        /** Json string. */
        String jsonStr;

        if (mIndex == -1 || mProcedure == null || mPropertyName == null) {
            return null;
        }

        jsonStr = mBaseUrl + deviceId + mProcedureStr + mProcedure;

        switch (mProcedure) {
            case "set":
                if (mDefaultValue == -1) {
                    return null;
                }
                return jsonStr + mParamStr + mPropertyName + mValueStr + mDefaultValue + mSetEndStr;
            case "get":
                return jsonStr + mParamStr + mPropertyName + mGetEndStr;
            default:
                return null;
        }
    }

    /**
     * Get JSON string.
     * @param deviceId DeviceId.
     * @param value Value.
     * @return JSON string.
     */
    String getJsonString(final String deviceId, final int value) {
        String jsonStr;

        if (mIndex == -1 || mProcedure == null || mPropertyName == null) {
            return null;
        }

        jsonStr = mBaseUrl + deviceId + mProcedureStr + mProcedure;

        switch (mProcedure) {
            case "set":
                return jsonStr + mParamStr + mPropertyName + mValueStr + value + mSetEndStr;
            case "get":
                return jsonStr + mParamStr + mPropertyName + mGetEndStr;
            default:
                return null;
        }
    }

    /**
     * Set Index.
     * @param index Index.
     */
    void setIndex(final int index) {
        mIndex = index;
    }

    /**
     * Set Procedure.
     * @param procedure Procedure.
     */
    void setProcedure(final String procedure) {
        mProcedure = procedure;
    }

    /**
     * Set Property name.
     * @param propertyName Property name.
     */
    void setPropertyName(final String propertyName) {
        mPropertyName = propertyName;
    }

    /**
     * Set Default value.
     * @param defaultValue Default value.
     */
    void setDefaultValue(final int defaultValue) {
        mDefaultValue = defaultValue;
    }
}
