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
     * Set Health device's RRInterval data
     * @param rrInterval Health device's RRInterval data
     */
    public void setRRInterval(final HeartData rrInterval) {
        mRRInterval = rrInterval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"tartgetDevice\": " + mTarget.toString() + ", ");
        builder.append("\"heartRate\": " + mHeartRate.toString() + ", ");
        builder.append("\"energyExpended\": " + mEnergyExpended.toString() + ", ");
        builder.append("\"RRInterval\": " + mRRInterval.toString() +  "} ");
        return builder.toString();
    }

    /**
     * Notify HeartRate data listener.
     */
    public interface OnHeartRateDataListener {
        /**
         * Notify HeartRate data.
         * @param device Hitoe device
         * @param notify HeartRate data
         */
        void onNotifyHeartRateData(final HitoeDevice device, final HeartRateData notify);
    }
}
