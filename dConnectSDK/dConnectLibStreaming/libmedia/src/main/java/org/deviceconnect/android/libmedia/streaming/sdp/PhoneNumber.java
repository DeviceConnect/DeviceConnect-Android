package org.deviceconnect.android.libmedia.streaming.sdp;

public class PhoneNumber {
    private String mNumber;

    public PhoneNumber() {
    }

    public PhoneNumber(String number) {
        mNumber = number;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    @Override
    public String toString() {
        return "p=" + mNumber;
    }
}
