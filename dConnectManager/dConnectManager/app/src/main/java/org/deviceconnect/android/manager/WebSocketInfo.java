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
    private String mRawId;
    private String mReceiverId;
    private String mUri;
    private long mConnectTime;

    public String getRawId() {
        return mRawId;
    }

    public void setRawId(final String id) {
        mRawId = id;
    }

    public String getReceiverId() {
        return mReceiverId;
    }

    public void setReceiverId(final String receiverId) {
        mReceiverId = receiverId;
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
