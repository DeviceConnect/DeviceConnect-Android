/*
 HeartRateData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.data;

/**
 * This class is information of a Heart Rate.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateData {
    private int mId;
    private int mHeartRate;
    private int mEnergyExpended;
    private double mRRInterval;

    public int getId() {
        return mId;
    }

    public void setId(final int id) {
        mId = id;
    }

    public int getHeartRate() {
        return mHeartRate;
    }

    public void setHeartRate(final int heartRate) {
        mHeartRate = heartRate;
    }

    public int getEnergyExpended() {
        return mEnergyExpended;
    }

    public void setEnergyExpended(final int energyExpended) {
        mEnergyExpended = energyExpended;
    }

    public double getRRInterval() {
        return mRRInterval;
    }

    public void setRRInterval(final double rrInterval) {
        mRRInterval = rrInterval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\": " + mId + ", ");
        builder.append("\"heartRate\": " + mHeartRate + ", ");
        builder.append("\"energyExpended\": " + mEnergyExpended + ", ");
        builder.append("\"RRInterval\": " + mRRInterval +  "} ");
        return builder.toString();
    }
}
