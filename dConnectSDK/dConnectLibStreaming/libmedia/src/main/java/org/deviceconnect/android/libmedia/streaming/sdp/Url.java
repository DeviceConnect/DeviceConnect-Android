package org.deviceconnect.android.libmedia.streaming.sdp;

public class Url {
    private String mUrl;

    public Url() {
    }

    public Url(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return "u=" + mUrl;
    }
}
