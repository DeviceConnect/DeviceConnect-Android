/*
 StressEstimationData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of Stress Estimation.
 * @author NTT DOCOMO, INC.
 */
public class StressEstimationData {
    /** LF/HF. */
    private double mLFHF;
    /** LF/HF's TimeStamp. */
    private long mTimeStamp;
    /** LF/HF's TimeStamp String. */
    private String mTimeStampString;

    /**
     * Get LFHF's value.
     * @return LFHF's value
     */
    public double getLFHFValue() {
        return mLFHF;
    }

    /**
     * Set LFHF's value.
     * @param lfhf LFHF's value
     */
    public void setLFHFValue(final double lfhf) {
        mLFHF = lfhf;
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
        builder.append("{\"lfhf\": " + mLFHF + ", ");
        builder.append("\"timeStamp\": " + mTimeStamp + ", ");
        builder.append("\"timeStampString\": " + mTimeStampString +  "} ");
        return builder.toString();
    }

    /**
     * Notify LFHF Data listener.
     */
    public interface OnLFHFDataListener {
        /**
         * Notify LFHF data.
         * @param device Hitoe device
         * @param notify LFHF data
         */
        void onNotifyLFHFData(final HitoeDevice device, final StressEstimationData notify);
    }
}
