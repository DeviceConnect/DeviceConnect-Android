/*
 LinkingDevice.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

public class LinkingDevice {

    private String mDisplayName;
    private String mName;
    private String mBdAddress;
    private Integer mModelId;
    private Integer mUniqueId;
    private byte[] mIllumination;
    private byte[] mVibration;
    private Object mSensor;
    private boolean mIsConnected;
    private int mFeature;

    public static final int LED = 1;
    public static final int GYRO = LED << 1;
    public static final int ACCELERATION = LED << 2;
    public static final int COMPASS = LED << 3;
    public static final int BATTERY = LED << 4;
    public static final int TEMPERATURE = LED << 5;
    public static final int HUMIDITY = LED << 6;

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(final String displayName) {
        mDisplayName = displayName;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getBdAddress() {
        return mBdAddress;
    }

    public void setBdAddress(final String bdAddress) {
        mBdAddress = bdAddress;
    }

    public Integer getModelId() {
        return mModelId;
    }

    public void setModelId(final Integer modelId) {
        mModelId = modelId;
    }

    public Integer getUniqueId() {
        return mUniqueId;
    }

    public void setUniqueId(final Integer uniqueId) {
        mUniqueId = uniqueId;
    }

    public byte[] getIllumination() {
        return mIllumination;
    }

    public void setIllumination(final byte[] illumination) {
        mIllumination = illumination;
    }

    public byte[] getVibration() {
        return mVibration;
    }

    public void setVibration(byte[] vibration) {
        mVibration = vibration;
    }

    public Object getSensor() {
        return mSensor;
    }

    public void setSensor(final Object sensor) {
        this.mSensor = sensor;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setIsConnected(final boolean isConnected) {
        mIsConnected = isConnected;
    }

    public int getFeature() {
        return mFeature;
    }

    public void setFeature(final int feature) {
        mFeature = feature;
    }

    public boolean isLED() {
        return (mFeature & LED) == LED;
    }

    public boolean isGyro() {
        return (mFeature & GYRO) == GYRO;
    }

    public boolean isAcceleration() {
        return (mFeature & ACCELERATION) == ACCELERATION;
    }

    public boolean isCompass() {
        return (mFeature & COMPASS) == COMPASS;
    }

    public boolean isBattery() {
        return (mFeature & BATTERY) == BATTERY;
    }

    public boolean isTemperature() {
        return (mFeature & TEMPERATURE) == TEMPERATURE;
    }

    public boolean isHumidity() {
        return (mFeature & HUMIDITY) == HUMIDITY;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof LinkingDevice)) {
            return false;
        }

        LinkingDevice device = (LinkingDevice) obj;
        return (device.mBdAddress.equals(mBdAddress) &&
                device.getModelId().equals(mModelId) &&
                device.getUniqueId().equals(mUniqueId));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mBdAddress == null) ? 0 : mBdAddress.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Address: " + getBdAddress() + "\n ModelId: " + getModelId() + "\nUniqueId: " + getUniqueId();
    }
}
