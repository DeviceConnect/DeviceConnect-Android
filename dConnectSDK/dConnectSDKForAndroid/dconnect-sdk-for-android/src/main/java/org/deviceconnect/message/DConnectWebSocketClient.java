/*
 DConnectWebSocketClient.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.net.Uri;
import android.util.Log;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.sdk.BuildConfig;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * WebSocketクライアントを管理するクラス.
 * @author NTT DOCOMO, INC.
 */
class DConnectWebSocketClient {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "DConnectSDK";

    /**
     * サービスIDとパスを接続する文字列.
     */
    private static final String JOIN_SERVICE_ID = "_";

    /**
     * Device Connect Managerのイベントを配送するリスナーを格納するマップ.
     */
    private final Map<String, List<HttpDConnectSDK.OnEventListener>> mListenerMap = new HashMap<>();

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
     * @param uri サーバへのURI
     * @param origin オリジン
     * @param accessToken アクセストークン
     */
    void connect(final String uri, final String origin, final String accessToken) {
        if (mWebSocketClient != null) {
            if (DEBUG) {
                Log.w(TAG, "WebSocketClient is already connected.");
            }
            return;
        }

        Map<String, String> headers = new HashMap<>();
        if (origin != null) {
            headers.put(IntentDConnectMessage.EXTRA_ORIGIN, origin);
        }

        mWebSocketClient = new WebSocketClient(URI.create(uri), new Draft_17(), headers, mTimeout) {
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
                                RuntimeException ex = new RuntimeException();
                                mOnWebSocketListener.onError(ex);
                            }
                        }
                    } else {
                        String key = createPath(json);
                        synchronized (mListenerMap) {
                            List<DConnectSDK.OnEventListener> listeners = mListenerMap.get(key);
                            if (listeners != null) {
                                for (DConnectSDK.OnEventListener l : listeners) {
                                    l.onMessage(new DConnectEventMessage(message));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    if (DEBUG) {
                        Log.w(TAG, "The message from Device Connect Manager is invalid.", e);
                    }

                    if (mOnWebSocketListener != null) {
                        mOnWebSocketListener.onError(e);
                    }
                }
            }

            @Override
            public void onOpen(final ServerHandshake handshake) {
                sendAccessToken(accessToken);
            }

            @Override
            public void onClose(final int code, final String reason, final boolean remote) {
                isEstablishedWebSocket = false;

                DConnectWebSocketClient.this.close();
                if (mOnWebSocketListener != null) {
                    mOnWebSocketListener.onClose();
                }
            }

            @Override
            public void onWebsocketPing(final WebSocket conn, final Framedata f) {
                super.onWebsocketPing(conn, f);
            }

            @Override
            public void onWebsocketPong(final WebSocket conn, final Framedata f) {
                super.onWebsocketPong(conn, f);
            }

            @Override
            public void onError(final Exception ex) {
                isEstablishedWebSocket = false;

                if (mOnWebSocketListener != null) {
                    mOnWebSocketListener.onError(ex);
                }
            }
        };

        if (uri.startsWith("wss")) {
            try {
                SSLSocketFactory factory = createSSLSocketFactory();
                mWebSocketClient.setSocket(factory.createSocket());
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
                if (mOnWebSocketListener != null) {
                    mOnWebSocketListener.onError(e);
                }
                return;
            }
        }
        mWebSocketClient.connect();
    }

    /**
     * WebSocketが接続中かを確認する.
     * @return 接続中の場合はtrue、それ以外はfalse
     */
    boolean isConnected() {
        return isEstablishedWebSocket;
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
        uri = uri.toLowerCase();
        if (json.has(DConnectMessage.EXTRA_SERVICE_ID)) {
            uri += JOIN_SERVICE_ID;
            uri += json.optString(DConnectMessage.EXTRA_SERVICE_ID);
        }
        return uri;
    }

    /**
     * URIからパスを抽出する.
     * @param uri パスを抽出するURI
     * @return パス
     */
    private String convertUriToPath(final Uri uri) {
        return uri.getPath().toLowerCase() + JOIN_SERVICE_ID + uri.getQueryParameter(DConnectMessage.EXTRA_SERVICE_ID);
    }

    /**
     * WebSocketを切断する.
     */
    void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
        isEstablishedWebSocket = false;
    }

    /**
     * 指定されたイベントのリスナーを持っているか確認します.
     *
     * @param uri イベントのURI
     * @return リスナーが存在する場合はtrue、それ以外はfalse
     */
    boolean hasEventListener(final Uri uri) {
        String key = convertUriToPath(uri);
        synchronized (mListenerMap) {
            List<DConnectSDK.OnEventListener> listeners = mListenerMap.get(key);
            return listeners != null && !listeners.isEmpty();
        }
    }

    /**
     * イベント通知リスナーを登録する.
     * @param uri 登録イベントのURI
     * @param listener 通知リスナー
     */
    void addEventListener(final Uri uri, final HttpDConnectSDK.OnEventListener listener) {
        String key = convertUriToPath(uri);
        synchronized (mListenerMap) {
            List<DConnectSDK.OnEventListener> listeners = mListenerMap.get(key);
            if (listeners == null) {
                listeners = new ArrayList<>();
                mListenerMap.put(key, listeners);
            }
            listeners.add(listener);
        }
    }

    /**
     * イベント通知リスナーのリストを削除する.
     * @param uri 解除するイベントのURI
     */
    void removeEventListener(final Uri uri) {
        String key = convertUriToPath(uri);
        synchronized (mListenerMap) {
            mListenerMap.remove(key);
        }
    }

    /**
     * イベント通知リスナーを削除する.
     *
     * @param uri 解除するイベントのURI
     * @param listener 削除するリスナー
     */
    void removeEventListener(final Uri uri, final HttpDConnectSDK.OnEventListener listener) {
        String key = convertUriToPath(uri);
        synchronized (mListenerMap) {
            List<DConnectSDK.OnEventListener> listeners = mListenerMap.get(key);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * アクセストークンをDevice Connect Managerに送信する.
     * @param accessToken アクセストークン
     */
    private void sendAccessToken(final String accessToken) {
        mWebSocketClient.send("{\"" + DConnectMessage.EXTRA_ACCESS_TOKEN + "\":\"" + accessToken + "\"}");
    }

    /**
     * SSL接続用のソケットを作成するファクトリークラスを作成します.
     *
     * @return SSLSocketFactory
     * @throws NoSuchAlgorithmException SSLに使用するアルゴリズムが存在しない場合に発生
     * @throws KeyManagementException
     */
    private SSLSocketFactory createSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] transManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, transManagers, new SecureRandom());
        return sslcontext.getSocketFactory();
    }
}
