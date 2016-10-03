/*
 BatteryData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class BatteryData {
    private long mTimeStamp;
    private boolean mLowBatteryFlag;
    private float mLevel;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public boolean isLowBatteryFlag() {
        return mLowBatteryFlag;
    }

    public void setLowBatteryFlag(final boolean lowBatteryFlag) {
        mLowBatteryFlag = lowBatteryFlag;
    }

    public float getLevel() {
        return mLevel;
    }

    public void setLevel(final float level) {
        mLevel = level;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LowBattery: ").append(isLowBatteryFlag()).append("\n")
        .append("BatteryLevel: ").append(getLevel());
        return sb.toString();
    }
}
