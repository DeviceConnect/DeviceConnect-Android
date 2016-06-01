package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class RawData {
    private long mTimeStamp;
    private int mValue;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RawData: ").append(getValue()).append("\n");
        return sb.toString();
    }
}
