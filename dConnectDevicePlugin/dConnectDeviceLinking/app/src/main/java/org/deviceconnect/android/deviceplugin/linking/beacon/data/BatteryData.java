package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class BatteryData {
    private long mTimeStamp;
    private boolean mLowBatteryFlag;
    private float mLevel;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public boolean isLowBatteryFlag() {
        return mLowBatteryFlag;
    }

    public void setLowBatteryFlag(boolean lowBatteryFlag) {
        mLowBatteryFlag = lowBatteryFlag;
    }

    public float getLevel() {
        return mLevel;
    }

    public void setLevel(float level) {
        mLevel = level;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LowBattery: ").append(isLowBatteryFlag()).append("\n")
        .append("BatteryLevel: ").append(getLevel()).append("\n");
        return sb.toString();
    }
}
