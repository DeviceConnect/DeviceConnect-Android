/*
 HeartRateData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of a Heart Rate.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateData {
    /** Health device info. */
    private TargetDeviceData mTarget;
    /** Health device's heartrate data. */
    private HeartData mHeartRate;
    /** Health device's RRInterval. */
    private HeartData mRRInterval;
    /** Health device's EnergyExpended. */
    private HeartData mEnergyExpended;
    /** Health device's ECG. */
    private HeartData mECG;

    /**
     * Get Health device's info.
     * @return Health device's info
     */
    public TargetDeviceData getDevice() {
        return mTarget;
    }

    /**
     * Set Health device's info.
     * @param target Health device's info
     */
    public void setDevice(final TargetDeviceData target) {
        mTarget = target;
    }

    /**
     * Get Health device's heartrate data.
     * @return Health device's heartrate data
     */
    public HeartData getHeartRate() {
        return mHeartRate;
    }

    /**
     * Set Health device's heartrate data.
     * @param heartRate Health device's heartrate data
     */
    public void setHeartRate(final HeartData heartRate) {
        mHeartRate = heartRate;
    }

    /**
     * Get Health device's EnergyExpended data.
     * @return Health device's Energy Expended data
     */
    public HeartData getEnergyExpended() {
        return mEnergyExpended;
    }

    /**
     * Set Health device's Energy Expended data.
     * @param energyExpended Health device's Energy Expended data
     */
    public void setEnergyExpended(final HeartData energyExpended) {
        mEnergyExpended = energyExpended;
    }

    /**
     * Get Health device's RRInterval data.
     * @return Health device's RRInterval data
     */
    public HeartData getRRInterval() {
        return mRRInterval;
    }


    /**
     * Set Health device's RRInterval data.
     * @param rrInterval Health device's RRInterval data
     */
    public void setRRInterval(final HeartData rrInterval) {
        mRRInterval = rrInterval;
    }
    /**
     * Set Health device's ecg data.
     * @param ecg Health device's ecg data
     */
    public void setECG(final HeartData ecg) {
        mECG = ecg;
    }
    /**
     * Get Health device's ECG data.
     * @return Health device's ECG data
     */
    public HeartData getECG() {
        return mECG;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"heart\":{");
        builder.append("\"device\":").append(mTarget.toString()).append(", ");
        builder.append("\"rate\":").append(mHeartRate.toString()).append(", ");
        builder.append("\"rr\":").append(mRRInterval.toString()).append(", ");
        builder.append("\"energy\":").append(mEnergyExpended.toString()).append("}} ");
        return builder.toString();
    }
}
