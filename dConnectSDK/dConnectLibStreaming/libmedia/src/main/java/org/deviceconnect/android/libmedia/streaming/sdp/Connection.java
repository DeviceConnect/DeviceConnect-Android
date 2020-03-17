package org.deviceconnect.android.libmedia.streaming.sdp;

public class Connection {

    private String mNetType;
    private String mAddrType;
    private String mAddress;

    public Connection() {
    }

    public Connection(String netType, String addrType, String address) {
        mNetType = netType;
        mAddrType = addrType;
        mAddress = address;
    }

    public Connection(String line) {
        String[] params = line.split(" ");
        mNetType = params[0];
        mAddrType = params[1];
        mAddress = params[2];
    }

    public String getNetType() {
        return mNetType;
    }

    public void setNetType(String netType) {
        mNetType = netType;
    }

    public String getAddrType() {
        return mAddrType;
    }

    public void setAddrType(String addrType) {
        mAddrType = addrType;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public String toString() {
        return "c=" + mNetType + " " + mAddrType + " " + mAddress;
    }
}
