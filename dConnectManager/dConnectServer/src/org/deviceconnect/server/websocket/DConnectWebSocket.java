/*
 DConnectWebSocket.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.websocket;

/**
 * WebSocketインターフェース.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface DConnectWebSocket {

    /**
     * クライアントにイベントメッセージを送信します.
     *
     * @param message イベントメッセージ
     */
    void sendMessage(String message);

    /**
     * WebSocketを切断します.
     */
    void disconnect();

    /**
     * WebSocket IDを取得します.
     * @return WebSocket ID
     */
    String getId();

    /**
     * 接続用URIを取得します.
     * @return 接続用URI
     */
    String getUri();

    /**
     * 接続要求元のオリジンを取得します.
     * @return オリジン
     */
    String getClientOrigin();
}
