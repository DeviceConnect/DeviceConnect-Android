package org.deviceconnect.android.libmedia.streaming.sdp;

public class SessionName {

    private String mName;

    public SessionName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return "s=" + mName;
    }
}
