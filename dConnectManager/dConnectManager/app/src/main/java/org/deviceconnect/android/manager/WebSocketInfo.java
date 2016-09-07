package org.deviceconnect.android.manager;

public class WebSocketInfo {
    private String mId;
    private String mEventKey;
    private String mUri;
    private String mOrigin;
    private long mConnectTime;

    public String getId() {
        return mId;
    }

    public void setId(final String id) {
        mId = id;
    }

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

    public String getOrigin() {
        return mOrigin;
    }

    public void setOrigin(final String origin) {
        mOrigin = origin;
    }

    public long getConnectTime() {
        return mConnectTime;
    }

    public void setConnectTime(final long connectTime) {
        mConnectTime = connectTime;
    }
}
