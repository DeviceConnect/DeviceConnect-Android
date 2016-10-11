/*
 AtmosphericPressureData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class AtmosphericPressureData {
    private long mTimeStamp;
    private float mValue;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(final float value) {
        mValue = value;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AtmosphericPressure: ").append(getValue());
        return sb.toString();
    }
}
