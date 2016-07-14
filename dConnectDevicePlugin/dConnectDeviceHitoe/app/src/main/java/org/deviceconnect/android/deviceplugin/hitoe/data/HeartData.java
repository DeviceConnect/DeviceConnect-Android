/*
 RateData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import android.os.Bundle;

import org.deviceconnect.android.profile.HealthProfile;

/**
 * This class is information of  HeartRate or RRI or Energy Expended or ECG.
 * @author NTT DOCOMO, INC.
 */
public class HeartData {
    /** HeartRate type.*/
    public enum HeartRateType {
        /**
         * HeartRate.
         */
        Rate,
        /**
         * RRI.
         */
        RRI,
        /**
         * Energy Expended.
         */
        EnergyExpended,
        /**
         * ECG.
         */
        ECG

    }

    /** HeartRate type. */
    private HeartRateType mHeartRateType;
    /** value. */
    private float mValue;
    /** MDER Float value. */
    private String mMderFloat;
    /** type. */
    private String mType;
    /** type code. */
    private int mTypeCode;
    /** unit. */
    private String mUnit;
    /** unit code. */
    private int mUnitCode;
    /** timestamp. */
    private long mTimeStamp;
    /** timestamp string. */
    private String mTimeStampString;

    /**
     * Constructor.
     */
    public HeartData() {
        mMderFloat = "";
        mType = "";
        mUnit = "";
        mTimeStampString = "";
    }

    /**
     * Get Heart's value.
     * @return heart's value
     */
    public float getValue() {
        return mValue;
    }

    /**
     * Set Heart's value.
     * @param value heart's value
     */
    public void setValue(final float value) {
        mValue = value;
    }

    /**
     * Get MDER Float value.
     * @return MDER Float value
     */
    public String getMderFloat() {
        return mMderFloat;
    }

    /**
     * Set MDER Float value.
     * @param mderFloat MDER Float value
     */
    public void setMderFloat(final String mderFloat) {
        mMderFloat = mderFloat;
    }

    /**
     * Get Value's type.
     * @return value's type
     */
    public String getType() {
        return mType;
    }

    /**
     * Set Value's type.
     * @param type value's type
     */
    public void setType(final String type) {
        mType = type;
    }

    /**
     * Get Value's type code.
     * @return Value's type code
     */
    public int getTypeCode() {
        return mTypeCode;
    }

    /**
     * Set Value's type code.
     * @param typeCode value's type code
     */
    public void setTypeCode(final int typeCode) {
        mTypeCode = typeCode;
    }

    /**
     * Get HeartRate's unit.
     * @return HeartRate's  unit
     */
    public String getUnit() {
        return mUnit;
    }

    /**
     * Set HeartRate's unit.
     * @param unit HeartRate's unit
     */
    public void setUnit(final String unit) {
        mUnit = unit;
    }

    /**
     * Get HeartRate's unit code.
     * @return HeartRate's unit code
     */
    public int getUnitCode() {
        return mUnitCode;
    }

    /**
     * Set HeartRate's unit code.
     * @param unitCode HeartRate's unit code
     */
    public void setUnitCode(final int unitCode) {
        mUnitCode = unitCode;
    }

    /**
     * Get HeartRate's timestamp.
     * @return HeartRate's timestamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Set HeartRate's timestamp.
     * @param timeStamp HeartRate's timestamp
     */
    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * Get HeartRate's timestamp string.
     * @return HeartRate's timestamp string
     */
    public String getTimeStampString() {
        return mTimeStampString;
    }

    /**
     * Set HeartRate's timestamp string.
     * @param timeStampString HeartRate's timestamp string
     */
    public void setTimeStampString(final String timeStampString) {
        mTimeStampString = timeStampString;
    }

    /**
     * Get HeartRate type.
     * @return HeartRate type
     */
    public HeartRateType getHeartRateType() {
        return mHeartRateType;
    }

    /**
     * Set HeartRate type.
     * @param heartRateType HeartRate type
     */
    public void setHeartRateType(final HeartRateType heartRateType) {
        mHeartRateType = heartRateType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"value\": ").append(mValue).append(", ");
        builder.append("\"MderFloat\": ").append(mMderFloat).append(", ");
        builder.append("\"type\": ").append(mType).append(", ");
        builder.append("\"typeCode\": ").append(mTypeCode).append(", ");
        builder.append("\"unit\": ").append(mUnit).append(", ");
        builder.append("\"unitCode\": ").append(mUnitCode).append(", ");
        builder.append("\"timeStamp\": ").append(mTimeStamp).append(", ");
        builder.append("\"timeStampString\": ").append(mTimeStampString).append("} ");
        return builder.toString();
    }

    /**
     * To Bundle.
     * @return  Bundle
     */
    public Bundle toBundle() {
        Bundle heart = new Bundle();
        HealthProfile.setValue(heart, mValue);
        HealthProfile.setMDERFloat(heart, mMderFloat);
        HealthProfile.setType(heart, mType);
        HealthProfile.setTypeCode(heart, mTypeCode);
        HealthProfile.setUnit(heart, mUnit);
        HealthProfile.setUnitCode(heart, mUnitCode);
        HealthProfile.setTimestamp(heart, mTimeStamp);
        HealthProfile.setTimestampString(heart, mTimeStampString);
        return heart;
    }
}
