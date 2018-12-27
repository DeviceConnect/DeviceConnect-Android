/*
 WebSocketInfo.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core;

import org.deviceconnect.server.websocket.DConnectWebSocket;

/**
 * イベント送信経路情報(WebSocket).
 *
 * @author NTT DOCOMO, INC.
 */
public class WebSocketInfo {
    /**
     * WebSocketの識別子.
     * <p>
     * {@link DConnectWebSocket#getId()}の値が入ります。
     * </p>
     */
    private String mRawId;

    /**
     * WebSocketのオリジン、もしくはセッションキー.
     * <p>
     * 旧仕様では、sessionKeyの値が入ります。<br>
     * 新仕様(GotAPI)では、originの値が入ります。
     * </p>
     */
    private String mOrigin;

    /**
     * WebSocketのURI.
     * <p>
     * 「origin + uri」の文字列が入ります。
     * </p>
     */
    private String mUri;

    /**
     * 接続が開始された時間.
     */
    private long mConnectTime;

    /**
     * WebSocketの識別子を取得する.
     * @return WebSocketの識別子
     */
    public String getRawId() {
        return mRawId;
    }

    /**
     * WebSocketの識別子を設定する.
     * @param id WebSocketの識別子
     */
    public void setRawId(final String id) {
        mRawId = id;
    }

    /**
     * WebSocketのオリジン、もしくはセッションキーを取得する.
     * <p>
     * 旧仕様では、sessionKeyを取得し、新仕様(GotAPI)では、originを取得する。
     * </p>
     * @return WebSocketのオリジン、もしくはセッションキー
     */
    public String getOrigin() {
        return mOrigin;
    }

    /**
     * WebSocketのオリジン、もしくはセッションキーを設定する.
     * @param origin WebSocketのオリジン、もしくはセッションキー
     */
    public void setOrigin(final String origin) {
        mOrigin = origin;
    }

    /**
     * WebSocketのURIを取得する.
     * @return WebSocketのURI
     */
    public String getUri() {
        return mUri;
    }

    /**
     * WebSocketのURIを設定する.
     * @param uri WebSocketのURI
     */
    public void setUri(final String uri) {
        mUri = uri;
    }

    /**
     * WebSocketの接続開始時間を取得する.
     * @return WebSocketの接続開始時間
     */
    public long getConnectTime() {
        return mConnectTime;
    }

    /**
     * WebSocketの接続開始時間を設定する.
     * @param connectTime WebSocketの接続開始時間
     */
    public void setConnectTime(final long connectTime) {
        mConnectTime = connectTime;
    }

    @Override
    public String toString() {
        return "WebSocketInfo: {\n" +
                "    mRawId: " + mRawId + "\n" +
                "    mOrigin: " + mOrigin + "\n" +
                "    mUri: " + mUri + "\n" +
                "}";
    }
}
