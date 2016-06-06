/*
 ECGData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of ECG.
 * @author NTT DOCOMO, INC.
 */
public class ECGData {
    /** ECG's Value. */
    private double mECGValue;
    /** ECG's TimeStamp. */
    private long mTimeStamp;
    /** ECG's TimeStamp String. */
    private String mTimeStampString;

    /**
     * Get ECG's value.
     * @return ECG's value
     */
    public double getECGValue() {
        return mECGValue;
    }

    /**
     * Set ECG's value.
     * @param ecgValue ECG's value
     */
    public void setECGValue(final double ecgValue) {
        mECGValue = ecgValue;
    }

    /**
     * Get TimeStamp.
     * @return TimeStamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Set TimeStamp.
     * @param timeStamp TimeStamp
     */
    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * Get TimeStamp String.
     * @return TimeStamp String
     */
    public String getTimeStampString() {
        return mTimeStampString;
    }

    /**
     * Set TimeStamp String.
     * @param timeStampString TimeStamp String
     */
    public void setTimeStampString(final String timeStampString) {
        mTimeStampString = timeStampString;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"ecg\": " + mECGValue + ", ");
        builder.append("\"timeStamp\": " + mTimeStamp + ", ");
        builder.append("\"timeStampString\": " + mTimeStampString +  "} ");
        return builder.toString();
    }

    /**
     * ECG Data Notify listener.
     */
    public interface OnNotifyECGDataListener {
        /**
         * Notify ECG Data.
         * @param device hitoe device
         * @param notify ECG data
         */
        void onNotifyECGData(final HitoeDevice device, final ECGData notify);
    }
}
