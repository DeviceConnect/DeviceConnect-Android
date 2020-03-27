package org.deviceconnect.android.libmedia.streaming.sdp;

public class Bandwidth {

    private String mType;
    private String mBandwidth;

    public Bandwidth() {
    }

    public Bandwidth(String type, String bandwidth) {
        mType = type;
        mBandwidth = bandwidth;
    }

    public Bandwidth(String line) {
        String[] params = line.split(":");
        mType = params[0].trim();
        mBandwidth = params[1].trim();
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getBandwidth() {
        return mBandwidth;
    }

    public void setBandwidth(String bandwidth) {
        mBandwidth = bandwidth;
    }

    public void setBandwidth(Integer bandwidth) {
        mBandwidth = String.valueOf(bandwidth);
    }

    @Override
    public String toString() {
        return "b=" + mType + ":" + mBandwidth;
    }
}
