package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class LinkingBeacon {

    private boolean mOnline;

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
        return "Linking:ビーコン(" + getExtraId() + ")";
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

    public void setOnline(boolean online) {
        mOnline = online;
    }

    public int getExtraId() {
        return mExtraId;
    }

    public void setExtraId(int extraId) {
        mExtraId = extraId;
    }

    public int getVendorId() {
        return mVendorId;
    }

    public void setVendorId(int vendorId) {
        mVendorId = vendorId;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public GattData getGattData() {
        return mGattData;
    }

    public void setGattData(GattData gattData) {
        mGattData = gattData;
    }

    public TemperatureData getTemperatureData() {
        return mTemperatureData;
    }

    public void setTemperatureData(TemperatureData temperatureData) {
        mTemperatureData = temperatureData;
    }

    public HumidityData getHumidityData() {
        return mHumidityData;
    }

    public void setHumidityData(HumidityData humidityData) {
        mHumidityData = humidityData;
    }

    public BatteryData getBatteryData() {
        return mBatteryData;
    }

    public void setBatteryData(BatteryData batteryData) {
        mBatteryData = batteryData;
    }

    public AtmosphericPressureData getAtmosphericPressureData() {
        return mAtmosphericPressureData;
    }

    public void setAtmosphericPressureData(AtmosphericPressureData atmosphericPressureData) {
        mAtmosphericPressureData = atmosphericPressureData;
    }

    public RawData getRawData() {
        return mRawData;
    }

    public void setRawData(RawData rawData) {
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
                .append(getBatteryData())
                .append(getHumidityData())
                .append(getTemperatureData())
                .append(getAtmosphericPressureData())
                .append(getRawData());
        return sb.toString();
    }
}
