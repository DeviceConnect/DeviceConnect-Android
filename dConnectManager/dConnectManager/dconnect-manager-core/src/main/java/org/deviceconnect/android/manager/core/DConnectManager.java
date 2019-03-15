/*
 DConnectManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Looper;
import android.os.RemoteException;

import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.manager.core.event.AbstractEventSessionFactory;
import org.deviceconnect.android.manager.core.event.EventSession;
import org.deviceconnect.android.manager.core.event.KeepAliveManager;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.ssl.EndPointKeyStoreManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.android.ssl.KeyStoreManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.DConnectServerError;
import org.deviceconnect.server.DConnectServerEventListener;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;
import org.deviceconnect.server.websocket.DConnectWebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.oauth.PackageInfoOAuth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Device Connect Manager クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectManager implements DConnectInterface {
    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * このインスタンスが属するコンテキスト.
     */
    private final Context mContext;

    /**
     * Device Connect サーバの設定.
     */
    private DConnectSettings mSettings;

    /**
     * イベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * キーストア管理オブジェクト.
     */
    private KeyStoreManager mKeyStoreMgr;

    /**
     * RESTfulサーバ.
     */
    private DConnectServer mRESTServer;

    /**
     * WebSocket管理クラス.
     */
    private WebSocketInfoManager mWebSocketInfoManager;

    /**
     * スレッドプール.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * Device Connect Managerの処理を行うクラス.
     */
    private DConnectCore mCore;

    /**
     * ネットワークの接続状態の変化を受け取るレシーバー.
     */
    private final BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            postChangedNetwork();
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @throws IllegalArgumentException コンテキストがnullの場合に発生
     */
    public DConnectManager(final Context context, final DConnectSettings settings) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }

        if (settings == null) {
            throw new IllegalArgumentException("settings is null.");
        }

        mContext = context;
        mSettings = settings;
        mKeyStoreMgr = new EndPointKeyStoreManager(context, DConnectConst.KEYSTORE_FILE_NAME);
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Device Connect サーバの設定を取得します.
     *
     * @return DConnectSettingsのインスタンス
     */
    public DConnectSettings getSettings() {
        return mSettings;
    }
    /**
     * イベントKeepAlive管理クラスを取得します.
     *
     * @return イベントKeepAlive管理クラス
     */
    public KeepAliveManager getKeepAliveManager() {
        return mCore.getKeepAliveManager();
    }

    /**
     * プラグイン管理クラスを取得します.
     *
     * @return プラグイン管理クラス
     */
    public DevicePluginManager getPluginManager() {
        return mCore.getPluginManager();
    }

    /**
     * KeyStore管理クラスを取得します.
     *
     * @return KeyStore管理クラス
     */
    public KeyStoreManager getKeyStoreManager() {
        return mKeyStoreMgr;
    }

    /**
     * WebSocket情報管理クラスを取得します.
     *
     * @return WebSocket情報管理クラス
     */
    public WebSocketInfoManager getWebSocketInfoManager() {
        return mWebSocketInfoManager;
    }

    /**
     * イベント通知用のリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * Device Connect サーバを起動します.
     */
    public void initDConnect() {
        mCore = new DConnectCore(mContext, mSettings, mEventSessionFactory);
        mCore.setDConnectInterface(this);
        mCore.setIDConnectCallback(new IDConnectCallback.Stub() {
            @Override
            public void sendMessage(final Intent message) throws RemoteException {
                try {
                    if (DConnectUtil.checkActionResponse(message)) {
                        handleResponse(message);
                    }
                } catch (Exception e) {
                    // ignore.
                }
            }
        });
        mCore.start();

        mExecutor.execute(() -> {
            mCore.searchPlugin();
            postFinishSearchPlugin();
        });
    }
    public void startDConnect() {
        initDConnect();
        mExecutor.execute(() -> {
            if (mRESTServer != null) {
                return;
            }
            if (!mSettings.isSSL()) {
                // SSL は認証局用のサービスが起動するため、それを行わないようにする
                startRESTServer(null);
            } else {
                mKeyStoreMgr.requestKeyStore(DConnectUtil.getIPAddress(getContext()), new KeyStoreCallback() {
                    @Override
                    public void onSuccess(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
                        startRESTServer(keyStore);
                    }

                    @Override
                    public void onError(final KeyStoreError error) {
                        postError(new RuntimeException("Failed to start HTTPS server: " + error.name()));
                    }
                });
            }
        });
    }
    /**
     * Device Connect サーバを停止します.
     */
    public void stopDConnect() {
        mExecutor.execute(this::stopRESTServer);
    }
    public void finalizeDConnect() {
        if (mCore != null) {
            mCore.stop();
            mCore = null;
        }

        synchronized (mRequestMap) {
            mRequestMap.clear();
        }
    }

    /**
     * DConnect Manager の動作状況を確認します.
     *
     * @return 動作している場合はtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return mRESTServer != null && mRESTServer.isRunning();
    }

    /**
     * イベントセッションを作成するファクトリ.
     */
    private AbstractEventSessionFactory mEventSessionFactory = new AbstractEventSessionFactory() {
        @Override
        public EventSession createSession(final Intent request, final String serviceId,
                                          final String receiverId, final String pluginId) {
            String appType = request.getStringExtra(DConnectConst.EXTRA_INNER_TYPE);
            if (DConnectConst.INNER_TYPE_HTTP.equals(appType)) {
                String accessToken = DConnectProfile.getAccessToken(request);
                String profileName = DConnectProfile.getProfile(request);
                String interfaceName = DConnectProfile.getInterface(request);
                String attributeName = DConnectProfile.getAttribute(request);

                WebSocketEventSession session = new WebSocketEventSession();
                session.setAccessToken(accessToken);
                session.setReceiverId(receiverId);
                session.setServiceId(serviceId);
                session.setPluginId(pluginId);
                session.setProfileName(profileName);
                session.setInterfaceName(interfaceName);
                session.setAttributeName(attributeName);
                session.setContext(mContext);
                return session;
            } else {
                String accessToken = DConnectProfile.getAccessToken(request);
                String profileName = DConnectProfile.getProfile(request);
                String interfaceName = DConnectProfile.getInterface(request);
                String attributeName = DConnectProfile.getAttribute(request);

                ComponentName receiver = request.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER);
                BroadcastEventSession session = new BroadcastEventSession();
                session.setAccessToken(accessToken);
                session.setReceiverId(receiverId);
                session.setServiceId(serviceId);
                session.setPluginId(pluginId);
                session.setProfileName(profileName);
                session.setInterfaceName(interfaceName);
                session.setAttributeName(attributeName);
                session.setContext(mContext);
                session.setBroadcastReceiver(receiver);
                return session;
            }
        }
    };

    /**
     * BroadcastReceiver のイベントセッション.
     */
    private class BroadcastEventSession extends EventSession {
        /**
         * 送信先のレシーバーのコンポーネント名.
         */
        private ComponentName mBroadcastReceiver;

        /**
         * レシーバーのコンポーネント名を設定します.
         * @param broadcastReceiver レシーバーのコンポーネント名
         */
        public void setBroadcastReceiver(final ComponentName broadcastReceiver) {
            mBroadcastReceiver = broadcastReceiver;
        }

        @Override
        public String createKey() {
            StringBuilder result = new StringBuilder();
            result.append(getReceiverId())
                    .append(DConnectConst.SEPARATOR)
                    .append(getPluginId())
                    .append(DConnectConst.SEPARATOR_SESSION)
                    .append(mBroadcastReceiver);
            return result.toString();
        }

        @Override
        public void sendEvent(final Intent event) throws IOException {
            event.setComponent(mBroadcastReceiver);
            getContext().sendBroadcast(event);
        }
    }

    /**
     * WebSocket のイベントセッション.
     */
    private class WebSocketEventSession extends EventSession {
        /**
         * 現在のスレッドがメインスレッドか確認します.
         * @return メインスレッドの場合はtrue、それ以外はfalse
         */
        private boolean isMainThread() {
            return Thread.currentThread().equals(Looper.getMainLooper().getThread());
        }

        @Override
        public void sendEvent(final Intent event) throws IOException {
            String key = event.getStringExtra(IntentDConnectMessage.EXTRA_SESSION_KEY);
            if (key == null) {
                mLogger.warning("sendEvent: key is not specified.");
                return;
            }

            if (mRESTServer != null && mRESTServer.isRunning()) {
                WebSocketInfo info = getWebSocketInfo(key);
                if (info == null) {
                    mLogger.warning("sendEvent: webSocket is not found: key = " + key);
                    return;
                }

                DConnectWebSocket webSocket = mRESTServer.getWebSocket(info.getRawId());
                if (webSocket != null) {
                    if (BuildConfig.DEBUG) {
                        mLogger.info(String.format("sendEvent: %s extra: %s", key, event.getExtras()));
                    }

                    mExecutor.execute(() -> {
                        try {
                            JSONObject root = new JSONObject();
                            DConnectUtil.convertBundleToJSON(getSettings(), root, event.getExtras());
                            webSocket.sendMessage(root.toString());
                        } catch (Exception e) {
                            mLogger.warning("JSONException in sendMessage: " + e.toString());
                        }
                    });
                }
            }
        }
    }

    /**
     * リクエストを一時的に格納するマップ.
     */
    private final ConcurrentHashMap<Integer, ResponseHolder> mRequestMap = new ConcurrentHashMap<>();

    /**
     * リクエストとレスポンスを一時的に格納するクラス
     */
    private class ResponseHolder {
        /**
         * リクエスト.
         */
        private Intent mRequest;

        /**
         * レスポンス.
         */
        private Intent mResponse;

        /**
         * レスポンスを設定し、{@link #waitResponse()} で停止しているスレッドを解除します.
         *
         * @param response レスポンス
         */
        synchronized void setResponse(final Intent response) {
            mResponse = response;
            notify();
        }

        /**
         * レスポンスが存在するか確認します.
         *
         * @return レスポンスが存在する場合はtrue、それ以外はfalse
         */
        synchronized boolean hasResponse() {
            return mResponse != null;
        }

        /**
         * レスポンスが設定されるのを待ちます.
         *
         * @throws InterruptedException タイムアウトした場合に発生
         */
        synchronized void waitResponse() throws InterruptedException {
            wait(mSettings.getRequestTimeout());
        }
    }

    /**
     * DConnectCore からのレスポンスを処理します.
     *
     * @param response レスポンス
     */
    private void handleResponse(final Intent response) {
        int requestCode = response.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, Integer.MIN_VALUE);
        if (requestCode == Integer.MIN_VALUE) {
            return;
        }

        ResponseHolder holder = mRequestMap.get(requestCode);
        if (holder != null) {
            holder.setResponse(mCore.createResponseIntent(holder.mRequest, response));
        }
    }

    /**
     * リクエストを DConnectCore に送信して実行します.
     *
     * @param request リクエスト
     * @return レスポンス
     */
    private Intent executeRequest(final Intent request) {
        final int requestCode = UUID.randomUUID().hashCode();

        ResponseHolder holder = new ResponseHolder();
        holder.mRequest = request;
        holder.mRequest.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        try {
            mRequestMap.put(requestCode, holder);

            mCore.handleMessage(request);

            // 既にレスポンスが設定されている場合には wait せずに結果を返す
            if (!holder.hasResponse()) {
                try {
                    holder.waitResponse();
                } catch (InterruptedException e) {
                    // ignore.
                }
            }
        } finally {
            mRequestMap.remove(requestCode);
        }
        return holder.mResponse;
    }

    /**
     * RESTfulサーバを起動します.
     *
     * <p>
     * ソケットサーバを立ち上げるので、UIスレッドだと例外が発生してしまいます。
     * このメソッドを呼び出すときはスレッドをから呼び出すこと。
     * </p>
     *
     * @param keyStore 証明書を管理するキーストア
     */
    private void startRESTServer(final KeyStore keyStore) {
        try {
            final DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
            builder.port(mSettings.getPort()).isSsl(mSettings.isSSL())
                    .accessLog(mSettings.isEnableAccessLog())
                    .documentRootPath(mCore.getFileMgr().getBasePath().getAbsolutePath())
                    .cachePath(mCore.getFileMgr().getBasePath().getAbsolutePath());

            if (!mSettings.allowExternalIP()) {
                // ローカルからのアクセスは、デフォルトで許可する
                ArrayList<String> list = new ArrayList<>();
                list.add("127.0.0.1"); // ipv4
                list.add("::1");       // ipv6
                builder.ipWhiteList(list);
            }

            mWebSocketInfoManager = new WebSocketInfoManager();
            mWebSocketInfoManager.addOnWebSocketEventListener(mCore::sendTransmitDisconnectEvent);

            // KeyStoreが存在する場合には、SSLServerSocketFactoryを作成する
            SSLServerSocketFactory factory = null;
            if (keyStore != null) {
                factory = createSSLServerSocketFactory(keyStore);
            }

            mRESTServer = new DConnectServerNanoHttpd(builder.build(), getContext(), factory);
            mRESTServer.setServerEventListener(new DConnectServerEventListener() {
                @Override
                public boolean onReceivedHttpRequest(final HttpRequest request, final HttpResponse response) {
                    Intent requestIntent = DConnectHttpUtil.convertHttp2Intent(mContext, mCore.getFileMgr(), request, response);
                    if (requestIntent != null) {
                        Intent responseIntent = executeRequest(requestIntent);
                        try {
                            if (responseIntent == null) {
                                DConnectHttpUtil.setTimeoutResponse(response);
                            } else {
                                DConnectHttpUtil.convertResponse(mSettings, response, responseIntent);
                            }
                        } catch (JSONException e) {
                            DConnectHttpUtil.setJSONFormatError(response);
                        } catch (UnsupportedEncodingException e) {
                            DConnectHttpUtil.setUnknownError(response);
                        }
                    }
                    return true;
                }

                @Override
                public void onError(final DConnectServerError errorCode) {
                    postError(new RuntimeException(errorCode.name()));
                }

                @Override
                public void onServerLaunched() {
                    postStarted();
                    registerNetworkMonitoring();
                }

                @Override
                public void onWebSocketConnected(final DConnectWebSocket webSocket) {
                }

                @Override
                public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
                    if (mWebSocketInfoManager == null || mCore == null) {
                        return;
                    }

                    WebSocketInfo disconnected = null;
                    for (WebSocketInfo info : mWebSocketInfoManager.getWebSocketInfos()) {
                        if (info.getRawId().equals(webSocket.getId())) {
                            disconnected = info;
                            break;
                        }
                    }
                    if (disconnected != null) {
                        mCore.getEventBroker().removeEventSession(disconnected.getOrigin());
                        mWebSocketInfoManager.removeWebSocketInfo(disconnected.getOrigin());
                    }
                }

                @Override
                public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
                    try {
                        JSONObject json = new JSONObject(message);
                        String uri = webSocket.getUri();
                        String origin = webSocket.getClientOrigin();
                        String eventKey;

                        if (uri.equalsIgnoreCase("/gotapi/websocket")) { // MEMO パスの大文字小文字を無視
                            // NOTE: GotAPI 1.1 対応 (新仕様)
                            String accessToken = json.optString(DConnectMessage.EXTRA_ACCESS_TOKEN);
                            if (accessToken == null) {
                                mLogger.warning("onWebSocketMessage: accessToken is not specified");
                                sendError(webSocket, DConnectConst.WS_ERROR_CODE_NOT_FOUND_ACCESS_TOKEN,
                                        "accessToken is not specified.");
                                return;
                            }

                            if (requiresOrigin()) {
                                if (origin == null) {
                                    sendError(webSocket, DConnectConst.WS_ERROR_CODE_NOT_FOUND_ORIGIN,
                                            "origin is not specified.");
                                    return;
                                }
                                if (usesLocalOAuth() && !isValidAccessToken(accessToken, origin)) {
                                    sendError(webSocket, DConnectConst.WS_ERROR_CODE_ACCESS_TOKEN_INVALID,
                                            "accessToken is invalid.");
                                    return;
                                }
                            } else {
                                if (origin == null) {
                                    origin = DConnectConst.ANONYMOUS_ORIGIN;
                                }
                            }
                            eventKey = origin;

                            // NOTE: 既存のイベントセッションを保持する.
                            // GotAPIでは、1つの接続しか許容しないないので、既に接続されている場合には切断する。
                            if (mWebSocketInfoManager.getWebSocketInfo(eventKey) != null) {
                                sendError(webSocket, DConnectConst.WS_ERROR_CODE_ALREADY_ESTABLISHED,
                                        "already established.");
                                webSocket.disconnect();
                                return;
                            }
                            sendSuccess(webSocket);
                        } else {
                            // NOTE: Device Connect 対応 (旧仕様)
                            if (origin == null) {
                                origin = DConnectConst.ANONYMOUS_ORIGIN;
                            }
                            eventKey = json.optString(DConnectMessage.EXTRA_SESSION_KEY);

                            // NOTE: 既存のイベントセッションを破棄する.
                            if (mWebSocketInfoManager.getWebSocketInfo(eventKey) != null) {
                                disconnectWebSocketWithReceiverId(eventKey);
                            }
                        }

                        if (eventKey == null) {
                            mLogger.warning("onWebSocketMessage: Failed to generate eventKey: uri = " + uri +  ", origin = " + origin);
                            return;
                        }

                        mWebSocketInfoManager.addWebSocketInfo(eventKey, origin + uri, webSocket.getId());
                    } catch (Exception e) {
                        mLogger.warning("onWebSocketMessage: Failed to parse message as JSON object: " + message);

                        sendError(webSocket, 5, "An unknown error occurred in parsing message.");
                        webSocket.disconnect();
                    }
                }

                /**
                 * アクセストークンとOriginの組み合わせが妥当かチェックします.
                 * @param accessToken アクセストークン
                 * @param origin オリジン
                 * @return 妥当な場合はtrue、それ以外はfalse
                 */
                private boolean isValidAccessToken(final String accessToken, final String origin) {
                    ClientPackageInfo client = mCore.getLocalOAuth2Main().findClientPackageInfoByAccessToken(accessToken);
                    if (client == null) {
                        return false;
                    }
                    PackageInfoOAuth oauth = client.getPackageInfo();
                    return oauth != null && oauth.getPackageName().equals(origin);
                }

                /**
                 * Origin要求の設定を取得します.
                 * @return Originを要求する場合はtrue、それ以外はfalse
                 */
                private boolean requiresOrigin() {
                    return mSettings.requireOrigin();
                }

                /**
                 * Local OAuth設定を取得します.
                 * @return Local OAuthが有効の場合はtrue、それ以外はfalse
                 */
                private boolean usesLocalOAuth() {
                    return mSettings.isUseALocalOAuth();
                }

                /**
                 * WebSocketに成功メッセージを送信します.
                 *
                 * @param webSocket メッセージを送信するWebSocket
                 */
                private void sendSuccess(final DConnectWebSocket webSocket) {
                    webSocket.sendMessage("{\"result\":0}");
                }

                /**
                 * WebSocketにエラーを送信します.
                 *
                 * @param webSocket    エラーを送信するWebSocket
                 * @param errorCode    エラーコード
                 * @param errorMessage エラーメッセージ
                 */
                private void sendError(final DConnectWebSocket webSocket, final int errorCode, final String errorMessage) {
                    webSocket.sendMessage("{\"result\":1,\"errorCode\":" + errorCode + ",\"errorMessage\":\"" + errorMessage + "\"}");
                }
            });
            mRESTServer.start();

            if (BuildConfig.DEBUG) {
                mLogger.info("RESTful Server was Started.");
                mLogger.info("DConnectSettings: " + mSettings.toString());
            }
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to start HTTPS server.", e);
            postError(e);
        }
    }

    /**
     * RESTfulサーバを停止します.
     */
    private void stopRESTServer() {
        unregisterNetworkMonitoring();

        if (mRESTServer != null) {
            mRESTServer.shutdown();
            mRESTServer = null;
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("RESTful Server was Stopped.");
        }
    }

    /**
     * ネットワーク変更監視を開始します.
     */
    private void registerNetworkMonitoring() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mWiFiReceiver, filter);
    }

    /**
     * ネットワーク変更監視を停止します.
     */
    private void unregisterNetworkMonitoring() {
        try {
            mContext.unregisterReceiver(mWiFiReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * SSLServerSocketFactoryを作成します.
     *
     * @param keyStore キーストア
     * @return SSLServerSocketFactoryのインスタンス
     * @throws GeneralSecurityException SSLServerSocketFactoryの作成に失敗した場合に発生
     */
    private SSLServerSocketFactory createSSLServerSocketFactory(final KeyStore keyStore) throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "0000".toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext.getServerSocketFactory();
    }

    /**
     * 指定されたreceiverIdのWebSocketの情報を取得する.
     *
     * @param receiverId WebSocketの識別子
     * @return WebSocketの情報。指定された識別子のWebSocketが存在しない場合は{@code null}を返す。
     */
    private WebSocketInfo getWebSocketInfo(final String receiverId) {
        return mWebSocketInfoManager.getWebSocketInfo(receiverId);
    }

    /**
     * 指定したイベントレシーバーIDに対応するWebSocketを切断する.
     *
     * @param receiverId イベントレシーバーID.
     */
    public void disconnectWebSocketWithReceiverId(final String receiverId) {
        if (receiverId != null) {
            WebSocketInfo info = mWebSocketInfoManager.getWebSocketInfo(receiverId);
            if (info != null) {
                disconnectWebSocket(info.getRawId());
            } else {
                mLogger.warning("disconnectWebSocketWithReceiverId: WebSocketInfo is not found: key = " + receiverId);
            }
        }
    }

    /**
     * WebSocketを切断する.
     * <p>
     * NOTE: Android 7以降ではメインスレッド上で切断すると例外が発生する場合があるため、
     * 別スレッド上で実行している.
     * </p>
     * @param webSocketId 内部的に発行したWebSocket ID
     */
    public void disconnectWebSocket(final String webSocketId) {
        mExecutor.execute(() -> {
            if (webSocketId != null) {
                DConnectWebSocket webSocket = mRESTServer.getWebSocket(webSocketId);
                if (webSocket != null) {
                    webSocket.disconnect();
                }
            }
        });
    }

    /**
     * 外部からのメッセージを受け取る。
     * @param message メッセージ
     */
    public void onReceivedMessage(final Intent message) {
        if (mCore != null) {
            if (DConnectUtil.checkAction(message)) {
                mExecutor.execute(() -> {
                    int requestCode = message.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, -1);
                    ComponentName cn = message.getParcelableExtra(IntentDConnectMessage.EXTRA_RECEIVER);
                    Intent responseIntent = executeRequest(message);
                    responseIntent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
                    responseIntent.setComponent(cn);
                    mContext.sendBroadcast(responseIntent);
                });
            } else if (DConnectUtil.checkActionResponse(message)) {
                handleResponse(message);
            } else {
                mCore.onReceivedMessage(message);
            }
        }
    }

    /**
     * プラグインの検索完了を通知します.
     */
    private void postFinishSearchPlugin() {
        if (mOnEventListener != null) {
            mOnEventListener.onFinishSearchPlugin();
        }
    }

    /**
     * DConnectCore が開始されたことを通知します.
     */
    private void postStarted() {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted();
        }
    }

    /**
     * DConnectCore が停止されたことを通知します.
     */
    private void postStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped();
        }
    }

    /**
     * ネットワーク環境が変更されたことを通知します.
     */
    private void postChangedNetwork() {
        if (mOnEventListener != null) {
            mOnEventListener.onChangedNetwork();
        }
    }

    /**
     * リスナーにエラーを通知します.
     *
     * @param e エラー原因の例外
     */
    private void postError(final Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * イベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * プラグインの検索が完了したことを通知します.
         */
        void onFinishSearchPlugin();

        /**
         * DConnectCore が起動したことを通知します.
         */
        void onStarted();

        /**
         * DConnectCore が停止したことを通知します.
         */
        void onStopped();

        /**
         * ネットワークが変更されたことを通知します.
         */
        void onChangedNetwork();

        /**
         * DConnectCore内でエラーが発生した場合に通知します.
         *
         * @param e 発生したエラーの例外
         */
        void onError(Exception e);
    }
}
