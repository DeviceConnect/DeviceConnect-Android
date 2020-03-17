package org.deviceconnect.android.libmedia.streaming.sdp;

public class Origin {
    private String mUsername;
    private Long mSessId;
    private Long mSessVersion;
    private String mNettype;
    private String mAddrtype;
    private String mAddress;

    public Origin(String username, Long sessId, Long sessVersion, String nettype, String addrtype, String address) {
        mUsername = username;
        mSessId = sessId;
        mSessVersion = sessVersion;
        mNettype = nettype;
        mAddrtype = addrtype;
        mAddress = address;
    }

    public Origin(String line) {
        String[] attr = line.split(" ");
        setUsername(attr[0]);
        setSessId(Long.parseLong(attr[1]));
        setSessVersion(Long.parseLong(attr[2]));
        setNettype(attr[3]);
        setAddrtype(attr[4]);
        setAddress(attr[5]);
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public Long getSessId() {
        return mSessId;
    }

    public void setSessId(Long sessId) {
        mSessId = sessId;
    }

    public Long getSessVersion() {
        return mSessVersion;
    }

    public void setSessVersion(Long sessVersion) {
        mSessVersion = sessVersion;
    }

    public String getNettype() {
        return mNettype;
    }

    public void setNettype(String nettype) {
        mNettype = nettype;
    }

    public String getAddrtype() {
        return mAddrtype;
    }

    public void setAddrtype(String addrtype) {
        mAddrtype = addrtype;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public String toString() {
        return "o=" + mUsername + " " + mSessId + " " + mSessVersion + " " + mNettype + " " + mAddrtype + " " + mAddress;
    }
}
