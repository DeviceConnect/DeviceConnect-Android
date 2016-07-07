/*
 RawData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class RawData {
    private long mTimeStamp;
    private int mValue;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(final int value) {
        mValue = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RawData: ").append(getValue());
        return sb.toString();
    }
}
