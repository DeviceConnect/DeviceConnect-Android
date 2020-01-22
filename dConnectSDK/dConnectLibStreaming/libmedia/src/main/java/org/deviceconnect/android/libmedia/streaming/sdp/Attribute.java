package org.deviceconnect.android.libmedia.streaming.sdp;

public class Attribute {
    private String mField;
    private String mValue;

    public Attribute() {
    }

    public Attribute(String field) {
        mField = field;
    }

    public Attribute(String field, String value) {
        mField = field;
        mValue = value;
    }

    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    @Override
    public String toString() {
        String value = getValue();
        return "a=" + getField() + (value != null ? ":" + value : "");
    }
}
