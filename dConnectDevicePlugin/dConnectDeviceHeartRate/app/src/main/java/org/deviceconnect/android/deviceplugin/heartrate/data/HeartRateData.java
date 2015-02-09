/*
 HeartRateData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.data;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateData {
    private int mId;
    private int mHeartRate;
    private int mEnergyExpended;
    private float mRRInterval;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getHeartRate() {
        return mHeartRate;
    }

    public void setHeartRate(int heartRate) {
        mHeartRate = heartRate;
    }

    public int getEnergyExpended() {
        return mEnergyExpended;
    }

    public void setEnergyExpended(int energyExpended) {
        mEnergyExpended = energyExpended;
    }

    public float getRRInterval() {
        return mRRInterval;
    }

    public void setRRInterval(float RRInterval) {
        mRRInterval = RRInterval;
    }
}
