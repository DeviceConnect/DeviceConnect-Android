package org.deviceconnect.android.libmedia.streaming.sdp;

public class EMail {
    private String mAddress;

    public EMail() {
    }

    public EMail(String address) {
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public String toString() {
        return "e=" + mAddress;
    }
}
