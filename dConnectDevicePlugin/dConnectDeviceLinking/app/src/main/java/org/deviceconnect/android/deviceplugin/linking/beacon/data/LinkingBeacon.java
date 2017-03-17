/*
 LinkingBeacon.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;

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
    private ButtonData mButtonData;

    public String getServiceId() {
        return LinkingBeaconUtil.createServiceIdFromLinkingBeacon(this);
    }

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

    public ButtonData getButtonData() {
        return mButtonData;
    }

    public void setButtonData(ButtonData buttonData) {
        mButtonData = buttonData;
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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof LinkingBeacon)) {
            return false;
        }

        LinkingBeacon beacon = (LinkingBeacon) obj;
        return (beacon.mExtraId == mExtraId && beacon.mVendorId == mVendorId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mExtraId + mVendorId;
        return result;
    }
}
