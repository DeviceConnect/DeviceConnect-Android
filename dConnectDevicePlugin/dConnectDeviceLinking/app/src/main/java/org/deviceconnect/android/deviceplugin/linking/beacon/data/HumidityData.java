package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class HumidityData {
    private long mTimeStamp;
    private float mValue;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        mValue = value;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Humidity: ").append(getValue()).append("\n");
        return sb.toString();
    }
}
