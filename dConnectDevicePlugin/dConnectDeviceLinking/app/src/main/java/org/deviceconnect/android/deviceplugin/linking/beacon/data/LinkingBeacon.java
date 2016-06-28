/*
 LinkingBeacon.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class LinkingBeacon {

    private boolean mOnline;

    private String mDisplayName;

    private int mExtraId;
    private int mVendorId;
    private int mVersion;

    private GattData mGattData;
    private TemperatureData mTemperatureData;
    private HumidityData mHumidityData;
    private BatteryData mBatteryData;
    private AtmosphericPressureData mAtmosphericPressureData;
    private RawData mRawData;

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(final String displayName) {
        mDisplayName = displayName;
    }

    public long getTimeStamp() {
        if (mGattData != null) {
            return mGattData.getTimeStamp();
        }
        return 0;
    }

    public boolean isOnline() {
        return mOnline;
    }

    public void setOnline(final boolean online) {
        mOnline = online;
    }

    public int getExtraId() {
        return mExtraId;
    }

    public void setExtraId(final int extraId) {
        mExtraId = extraId;
    }

    public int getVendorId() {
        return mVendorId;
    }

    public void setVendorId(final int vendorId) {
        mVendorId = vendorId;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(final int version) {
        mVersion = version;
    }

    public GattData getGattData() {
        return mGattData;
    }

    public void setGattData(final GattData gattData) {
        mGattData = gattData;
    }

    public TemperatureData getTemperatureData() {
        return mTemperatureData;
    }

    public void setTemperatureData(final TemperatureData temperatureData) {
        mTemperatureData = temperatureData;
    }

    public HumidityData getHumidityData() {
        return mHumidityData;
    }

    public void setHumidityData(final HumidityData humidityData) {
        mHumidityData = humidityData;
    }

    public BatteryData getBatteryData() {
        return mBatteryData;
    }

    public void setBatteryData(final BatteryData batteryData) {
        mBatteryData = batteryData;
    }

    public AtmosphericPressureData getAtmosphericPressureData() {
        return mAtmosphericPressureData;
    }

    public void setAtmosphericPressureData(final AtmosphericPressureData atmosphericPressureData) {
        mAtmosphericPressureData = atmosphericPressureData;
    }

    public RawData getRawData() {
        return mRawData;
    }

    public void setRawData(final RawData rawData) {
        mRawData = rawData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExtraId: ")
                .append(getExtraId())
                .append("\n")
                .append("VendorId: ")
                .append(getVendorId())
                .append("\n")
                .append("Version: ")
                .append(getVersion())
                .append("\n")
                .append(getGattData())
                .append("\n")
                .append(getBatteryData())
                .append("\n")
                .append(getHumidityData())
                .append("\n")
                .append(getTemperatureData())
                .append("\n")
                .append(getAtmosphericPressureData())
                .append("\n")
                .append(getRawData());
        return sb.toString();
    }
}
