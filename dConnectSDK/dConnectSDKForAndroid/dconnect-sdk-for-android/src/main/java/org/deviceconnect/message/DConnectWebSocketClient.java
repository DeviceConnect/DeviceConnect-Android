/*
 DConnectWebSocketClient.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.net.Uri;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketクライアントを管理するクラス.
 * @author NTT DOCOMO, INC.
 */
class DConnectWebSocketClient {

    /**
     * Device Connect Managerのイベントを配送するリスナーを格納するマップ.
     */
    private Map<String, HttpDConnectSDK.OnEventListener> mListenerMap = new HashMap<>();

    /**
     * WebSocketの接続状態を通知するリスナー.
     */
    private HttpDConnectSDK.OnWebSocketListener mOnWebSocketListener;

    /**
     * WebSocketクライアント.
     */
    private WebSocketClient mWebSocketClient;

    /**
     * WebSocketの接続が確率フラグ.
     * <p>
     * trueの場合は接続済み、falseの場合は接続していない
     * </p>
     */
    private boolean isEstablishedWebSocket;

    /**
     * WebSocketタイムアウト時間(ms).
     */
    private int mTimeout = 30 * 1000;

    /**
     * WebSocketの接続状態を通知するリスナーを設定する.
     * @param onWebSocketListener 接続状態を通知するリスナー
     */
    void setOnWebSocketListener(final HttpDConnectSDK.OnWebSocketListener onWebSocketListener) {
        mOnWebSocketListener = onWebSocketListener;
    }

    /**
     * WebSocket接続タイムアウト時間(ms)を設定する.
     * @param timeout タイムアウト時間(ms)
     */
    void setTimeout(final int timeout) {
        mTimeout = timeout;
    }

    /**
     * Device Connect ManagerのWebSocketサーバに接続を行う.
     * @param origin オリジン
     * @param accessToken アクセストークン
     */
    synchronized void connect(final String origin, final String accessToken) {
        if (mWebSocketClient != null) {
            return;
        }

        Map<String, String> headers = new HashMap<>();
        if (origin != null) {
            headers.put(IntentDConnectMessage.EXTRA_ORIGIN, origin);
        }

        URI uri = URI.create("ws://localhost:4035/gotapi/websocket");
        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), headers, mTimeout) {
            @Override
            public void onMessage(final String message) {
                try {
                    JSONObject json = new JSONObject(message);
                    if (!isEstablishedWebSocket) {
                        if (json.getInt(DConnectMessage.EXTRA_RESULT) == DConnectMessage.RESULT_OK) {
                            isEstablishedWebSocket = true;
                            if (mOnWebSocketListener != null) {
                                mOnWebSocketListener.onOpen();
                            }
                        } else {
                            if (mOnWebSocketListener != null) {
                                mOnWebSocketListener.onError(null);
                            }
                        }
                    } else {
                        DConnectSDK.OnEventListener l = mListenerMap.get(createPath(json));
                        if (l != null) {
                            l.onMessage(new DConnectEventMessage(message));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(final ServerHandshake handshake) {
                sendAccessToken(accessToken);
            }

            @Override
            public void onClose(final int code, final String reason, final boolean remote) {
                if (mOnWebSocketListener != null) {
                    mOnWebSocketListener.onClose();
                }
            }

            @Override
            public void onError(final Exception ex) {
                if (mOnWebSocketListener != null) {
                    mOnWebSocketListener.onError(ex);
                }
            }
        };
        mWebSocketClient.connect();
    }

    /**
     * イベントのパスを作成する.
     * @param json イベントメッセージ
     * @return パス
     */
    private String createPath(final JSONObject json) {
        String uri = "/gotapi";
        if (json.has(DConnectMessage.EXTRA_PROFILE)) {
            uri += "/";
            uri +=  json.optString(DConnectMessage.EXTRA_PROFILE);
        }
        if (json.has(DConnectMessage.EXTRA_INTERFACE)) {
            uri += "/";
            uri += json.optString(DConnectMessage.EXTRA_INTERFACE);
        }
        if (json.has(DConnectMessage.EXTRA_ATTRIBUTE)) {
            uri += "/";
            uri += json.optString(DConnectMessage.EXTRA_ATTRIBUTE);
        }
        return uri.toLowerCase();
    }

    /**
     * URIからパスを抽出する.
     * @param uri パスを抽出するURI
     * @return パス
     */
    private String convertUriToPath(final String uri) {
        Uri u = Uri.parse(uri);
        return u.getPath().toLowerCase();
    }

    /**
     * WebSocketを切断する.
     */
    synchronized void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
    }

    /**
     * イベント通知リスナーを登録する.
     * @param uri 登録イベントのURI
     * @param listener 通知リスナー
     */
    void addEventListener(final String uri, final HttpDConnectSDK.OnEventListener listener) {
        mListenerMap.put(convertUriToPath(uri), listener);
    }

    /**
     * イベント通知リスナーを削除する.
     * @param uri 解除するイベントのURI
     */
    void removeEventListener(final String uri) {
        mListenerMap.remove(convertUriToPath(uri));
    }

    /**
     * アクセストークンをDevice Connect Managerに送信する.
     * @param accessToken アクセストークン
     */
    private void sendAccessToken(final String accessToken) {
        mWebSocketClient.send("{\"" + DConnectMessage.EXTRA_ACCESS_TOKEN + "\":\"" + accessToken + "\"}");
    }
}
