package org.deviceconnect.android.libmedia.streaming.sdp;

public class EncryptionKey {
    private String mMethod;
    private String mEncryptionKey;

    public EncryptionKey() {
    }

    public EncryptionKey(String method, String encryptionKey) {
        mMethod = method;
        mEncryptionKey = encryptionKey;
    }

    public EncryptionKey(String line) {
        int indexOf = line.indexOf(":");
        if (indexOf == -1) {
            mMethod = line.trim();
        } else {
            mMethod = line.substring(0, indexOf);
            mEncryptionKey = line.substring(indexOf + 1);
        }
    }

    public String getMethod() {
        return mMethod;
    }

    public void setMethod(String method) {
        mMethod = method;
    }

    public String getEncryptionKey() {
        return mEncryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        mEncryptionKey = encryptionKey;
    }

    @Override
    public String toString() {
        return "k=" + mMethod + (mEncryptionKey != null ? ":" + mEncryptionKey : "");
    }
}
