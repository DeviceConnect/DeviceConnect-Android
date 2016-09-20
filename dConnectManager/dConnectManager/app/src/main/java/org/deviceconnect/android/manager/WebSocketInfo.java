package org.deviceconnect.android.manager;

public class WebSocketInfo {
    private String mEventKey;
    private String mUri;
    private long mConnectTime;

    public String getEventKey() {
        return mEventKey;
    }

    public void setEventKey(final String eventKey) {
        mEventKey = eventKey;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(final String uri) {
        mUri = uri;
    }

    public long getConnectTime() {
        return mConnectTime;
    }

    public void setConnectTime(final long connectTime) {
        mConnectTime = connectTime;
    }
}
