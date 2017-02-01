package org.deviceconnect.android.uiapp.data;

import java.util.List;

public class DCParam {
    boolean mRequired;
    String mName;
    String mType;
    String mFormat;
    Number mMin;
    Number mMax;
    List mEnum;

    boolean mSend;
    String mValue;

    public boolean isRequired() {
        return mRequired;
    }

    public void setRequired(boolean required) {
        mRequired = required;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public Number getMin() {
        return mMin;
    }

    public void setMin(Number min) {
        mMin = min;
    }

    public Number getMax() {
        return mMax;
    }

    public void setMax(Number max) {
        mMax = max;
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        mFormat = format;
    }

    public boolean isSend() {
        return mSend;
    }

    public void setSend(boolean send) {
        mSend = send;
    }

    public List getEnum() {
        return mEnum;
    }

    public void setEnum(List anEnum) {
        mEnum = anEnum;
    }
}
