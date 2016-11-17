/*
 DConnectServerEventListener.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server;

import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.websocket.DConnectWebSocket;

/**
 * DConnectServerからのイベントを受け取る為のリスナーインターフェース.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface DConnectServerEventListener {

    /**
     * Httpリクエストの受信時に呼び出されます. <br/>
     * Device Connect のリクエストとして正しくないなどの理由でリクエストの受理を破棄する
     * 場合は、戻り値としてfalseを返すことでサーバーに通常のHTTPリクエストとして処理させます。
     * 
     * @param req Httpリクエスト
     * @param res Httpレスポンス
     * @return リクエストを受理する場合はtrue、リクエストを無視する場合はfalseを返す
     */
    boolean onReceivedHttpRequest(HttpRequest req, HttpResponse res);

    /**
     * サーバーでエラーが発生した場合に呼び出されます.
     * 
     * @param errorCode エラー種類
     */
    void onError(DConnectServerError errorCode);

    /**
     * サーバーが起動した時に呼び出されます.
     */
    void onServerLaunched();

    /**
     * WebSocketのセッションが接続された時に呼び出されます。
     * @param webSocket WebSocketのインスタンス
     */
    void onWebSocketConnected(DConnectWebSocket webSocket);

    /**
     * WebSocketのセッションが切断された時に呼び出されます.
     * @param webSocketId WebSocket ID
     */
    void onWebSocketDisconnected(String webSocketId);

    /**
     * WebSocketからのメッセージを受信した時に呼び出されます.
     * @param webSocket メッセージを受信したWebSocket
     * @param message 受信したメッセージ
     */
    void onWebSocketMessage(DConnectWebSocket webSocket, String message);

    /**
     * WebSocketのsessionKeyがリセットされた時に呼び出されます.
     * @param sessionKey リセットされたsessionKey
     */
    void onResetEventSessionKey(String sessionKey);
}
