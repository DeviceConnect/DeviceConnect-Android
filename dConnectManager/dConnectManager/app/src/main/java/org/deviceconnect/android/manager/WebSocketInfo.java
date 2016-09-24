/*
 WebSocketInfo.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

/**
 * イベント送信経路情報(WebSocket).
 *
 * @author NTT DOCOMO, INC.
 */
public class WebSocketInfo {
    private String mId;
    private String mEventKey;
    private String mUri;
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

    public long getConnectTime() {
        return mConnectTime;
    }

    public void setConnectTime(final long connectTime) {
        mConnectTime = connectTime;
    }
}
