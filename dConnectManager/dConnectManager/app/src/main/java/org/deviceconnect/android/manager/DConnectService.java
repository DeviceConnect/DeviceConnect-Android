/*
 DConnectService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.compat.CompatibleRequestConverter;
import org.deviceconnect.android.manager.compat.ServiceDiscoveryConverter;
import org.deviceconnect.android.manager.compat.ServiceInformationConverter;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.event.KeepAlive;
import org.deviceconnect.android.manager.event.KeepAliveManager;
import org.deviceconnect.android.manager.plugin.ConnectionType;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;
import org.deviceconnect.server.websocket.DConnectWebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * dConnect Manager本体.
 * @author NTT DOCOMO, INC.
 */
public class DConnectService extends DConnectMessageService implements WebSocketInfoManager.OnWebSocketEventListener {

    /**
     * WakeLockのタグを定義する.
     */
    private static final String TAG_WAKE_LOCK = "DeviceConnectManager";

    public static final String ACTION_DISCONNECT_WEB_SOCKET = "disconnect.WebSocket";
    public static final String ACTION_SETTINGS_KEEP_ALIVE = "settings.KeepAlive";
    public static final String EXTRA_WEBSOCKET_ID = "webSocketId";
    public static final String EXTRA_KEEP_ALIVE_ENABLED = "enabled";
    public static final String EXTRA_EVENT_RECEIVER_ID = "receiverId";

    /** 内部用: 通信タイプを定義する. */
    public static final String EXTRA_INNER_TYPE = "_type";
    /** 通信タイプがHTTPであることを示す定数. */
    public static final String INNER_TYPE_HTTP = "http";

    /** 内部用: アプリケーションタイプを定義する. */
    public static final String EXTRA_INNER_APP_TYPE = "_app_type";
    /** 通信相手がWebアプリケーションであることを示す定数. */
    public static final String INNER_APP_TYPE_WEB = "web";

    /** RESTfulサーバ. */
    private DConnectServer mRESTfulServer;

    /** RESTfulサーバからのイベントを受領するリスナー. */
    private DConnectServerEventListenerImpl mWebServerListener;

    /** WebSocket管理クラス. */
    private WebSocketInfoManager mWebSocketInfoManager;

    /** イベント送信スレッド. */
    private ExecutorService mEventSender = Executors.newSingleThreadExecutor();

    /** イベントKeep Alive管理クラス. */
    private KeepAliveManager mKeepAliveManager;

    /** リクエストのパスを変換するクラス群. */
    private MessageConverter[] mRequestConverters;

    /** レスポンスのパスを変換するクラス群. */
    private MessageConverter[] mResponseConverters;

    /** WakeLockのインスタンス. */
    private PowerManager.WakeLock mWakeLock;

    /** バインドするためのクラス. */
    private final IBinder mLocalBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWebSocketInfoManager = new WebSocketInfoManager();
        mWebSocketInfoManager.addOnWebSocketEventListener(this);

        mKeepAliveManager = new KeepAliveManager(this, mEventSessionTable);
        mEventBroker.setRegistrationListener(new EventBroker.RegistrationListener() {
            @Override
            public void onPutEventSession(final Intent request, final DevicePlugin plugin) {
                if (isSupportedKeepAlive(plugin)) {
                    mKeepAliveManager.setManagementTable(plugin);
                }
            }

            @Override
            public void onDeleteEventSession(final Intent request, final DevicePlugin plugin) {
                if (isSupportedKeepAlive(plugin)) {
                    mKeepAliveManager.removeManagementTable(plugin);
                }
            }
        });
        mRequestConverters = new MessageConverter[] {
                new CompatibleRequestConverter(getPluginManager())
        };
        mResponseConverters = new MessageConverter[] {
                new ServiceDiscoveryConverter(),
                new ServiceInformationConverter()
        };

        if (mSettings.isManagerStartFlag()) {
            startInternal();
        }
    }

    @Override
    public void onDestroy() {
        mWebSocketInfoManager.removeOnWebSocketEventListener(this);

        stopRESTfulServer();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            mLogger.warning("intent is null.");
            return START_STICKY;
        }
        String action = intent.getAction();
        if (action == null) {
            mLogger.warning("action is null.");
            return START_STICKY;
        }

        if (ACTION_DISCONNECT_WEB_SOCKET.equals(action)) {
            String webSocketId = intent.getStringExtra(EXTRA_WEBSOCKET_ID);
            disconnectWebSocket(webSocketId);
            return START_STICKY;
        }

        if (ACTION_SETTINGS_KEEP_ALIVE.equals(action)) {
            if (intent.getBooleanExtra(EXTRA_KEEP_ALIVE_ENABLED, true)) {
                mKeepAliveManager.enableKeepAlive();
            } else {
                mKeepAliveManager.disableKeepAlive();
            }
            return START_STICKY;
        }
        if (IntentDConnectMessage.ACTION_KEEPALIVE.equals(action)) {
            onKeepAliveCommand(intent);
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDisconnect(final String origin) {
        List<DevicePlugin> plugins = getPluginManager().getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            String serviceId = plugin.getPluginId();
            Intent request = new Intent();
            request.setComponent(plugin.getComponentName());
            request.setAction(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT);
            request.putExtra(SystemProfileConstants.PARAM_PLUGIN_ID, serviceId);
            request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, origin);
            try {
                plugin.send(request);
            } catch (MessagingException e) {
                mLogger.info("Failed to send the notification: ACTION_EVENT_TRANSMIT_DISCONNECT");
            }
        }
    }

    private void onKeepAliveCommand(final Intent intent) {
        String status = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS);
        if (status.equals("RESPONSE")) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                KeepAlive keepAlive = mKeepAliveManager.getKeepAlive(serviceId);
                if (keepAlive != null) {
                    keepAlive.setResponseFlag();
                }
            }
        } else if (status.equals("DISCONNECT")) {
            String receiverId = intent.getStringExtra(DConnectService.EXTRA_EVENT_RECEIVER_ID);
            if (receiverId != null) {
                disconnectWebSocketWithReceiverId(receiverId);
            }
        }
    }

    private boolean isSupportedKeepAlive(final DevicePlugin plugin) {
        if (plugin.getConnectionType() != ConnectionType.BROADCAST) {
            return false;
        }
        VersionName version = plugin.getPluginSdkVersionName();
        VersionName match = VersionName.parse("1.1.0");
        return !(version.compareTo(match) == -1);
    }

    @Override
    public void sendResponse(final Intent request, final Intent response) {
        Intent intent = createResponseIntent(request, response);
        if (INNER_TYPE_HTTP.equals(request.getStringExtra(EXTRA_INNER_TYPE))) {
            mWebServerListener.onResponse(intent);
        } else {
            sendBroadcast(intent);
        }
    }

    @Override
    public void sendEvent(final String receiver, final Intent event) {
        if (receiver == null || receiver.length() <= 0) {
            mEventSender.execute(new Runnable() {
                @Override
                public void run() {
                    String key = event.getStringExtra(IntentDConnectMessage.EXTRA_SESSION_KEY);
                    if (key != null && mRESTfulServer != null && mRESTfulServer.isRunning()) {
                        WebSocketInfo info = getWebSocketInfo(key);
                        if (info == null) {
                            mLogger.warning("sendMessage: webSocket is not found: key = " + key);
                            return;
                        }

                        try {
                            if (BuildConfig.DEBUG) {
                                mLogger.info(String.format("sendMessage: %s extra: %s", key, event.getExtras()));
                            }
                            JSONObject root = new JSONObject();
                            DConnectUtil.convertBundleToJSON(root, event.getExtras());
                            DConnectWebSocket webSocket = mRESTfulServer.getWebSocket(info.getRawId());
                            if (webSocket != null && mRESTfulServer.isRunning()) {
                                webSocket.sendMessage(root.toString());
                            } else {
                                if (mWebServerListener != null) {
                                    mWebServerListener.onWebSocketDisconnected(webSocket);
                                }
                            }
                        } catch (JSONException e) {
                            mLogger.warning("JSONException in sendMessage: " + e.toString());
                        }
                    }
                }
            });
        } else {
            super.sendEvent(receiver, event);
        }
    }

    /**
     * 指定したイベントレシーバーIDに対応するWebSocketを切断する.
     *
     * @param receiverId イベントレシーバーID.
     */
    public void disconnectWebSocketWithReceiverId(final String receiverId) {
        if (receiverId != null) {
            WebSocketInfo info = getWebSocketInfo(receiverId);
            if (info != null) {
                disconnectWebSocket(info.getRawId());
            } else {
                mLogger.warning("disconnectWebSocketWithReceiverId: WebSocketInfo is not found: key = " + receiverId);
            }
        }
    }

    /**
     * WebSocketを切断する.
     *
     * NOTE: Android 7以降ではメインスレッド上で切断すると例外が発生する場合があるため、
     * 別スレッド上で実行している.
     *
     * @param webSocketId 内部的に発行したWebSocket ID
     */
    private void disconnectWebSocket(final String webSocketId) {
        mEventSender.execute(new Runnable() {
            @Override
            public void run() {
                if (webSocketId != null) {
                    DConnectWebSocket webSocket = mRESTfulServer.getWebSocket(webSocketId);
                    if (webSocket != null) {
                        webSocket.disconnect();
                    }
                }
            }
        });
    }

    /**
     * {@link EventBroker}を取得する.
     * @return EventBrokerのインスタンス
     */
    public EventBroker getEventBroker() {
        return mEventBroker;
    }

    /**
     * 指定されたreceiverIdのWebSocketの情報を取得する.
     * @param receiverId WebSocketの識別子
     * @return WebSocketの情報。指定された識別子のWebSocketが存在しない場合は{@code null}を返す。
     */
    private WebSocketInfo getWebSocketInfo(final String receiverId) {
        return mWebSocketInfoManager.getWebSocketInfo(receiverId);
    }

    /**
     * HTTPサーバを開始する.
     */
    private void startRESTfulServer() {
        mEventSender.execute(new Runnable() {
            @Override
            public void run() {
                if (mRESTfulServer != null) {
                    return;
                }

                if (mSettings.enableWakLock()) {
                    acquireWakeLock();
                }

                mSettings.load(getApplicationContext());

                mWebServerListener = new DConnectServerEventListenerImpl(DConnectService.this);
                mWebServerListener.setFileManager(mFileMgr);

                DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
                builder.port(mSettings.getPort()).isSsl(mSettings.isSSL())
                        .documentRootPath(getFilesDir().getAbsolutePath())
                        .cachePath(mFileMgr.getBasePath().getAbsolutePath());

                if (!mSettings.allowExternalIP()) {
                    // ローカルからのアクセスは、デフォルトで許可する
                    ArrayList<String> list = new ArrayList<>();
                    list.add("127.0.0.1");
                    list.add("::1");
                    builder.ipWhiteList(list);
                }

                if (BuildConfig.DEBUG) {
                    mLogger.info("RESTful Server was Started.");
                    mLogger.info("DConnectSettings: " + mSettings.toString());
                }

                mRESTfulServer = new DConnectServerNanoHttpd(builder.build(), getApplicationContext());
                mRESTfulServer.setServerEventListener(mWebServerListener);
                mRESTfulServer.start();

                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(mWiFiReceiver, filter);
            }
        });
    }

    /**
     * HTTPサーバを停止する.
     */
    private void stopRESTfulServer() {
        mEventSender.execute(new Runnable() {
            @Override
            public void run() {
                releaseWakeLock();

                if (mRESTfulServer != null) {
                    unregisterReceiver(mWiFiReceiver);
                    mRESTfulServer.shutdown();
                    mRESTfulServer = null;
                }

                if (BuildConfig.DEBUG) {
                    mLogger.info("RESTful Server was Stopped.");
                }
            }
        });
    }

    /**
     * DConnectManagerを起動する.
     */
    public synchronized void startInternal() {
        if (!mRunningFlag) {
            mRunningFlag = true;
            startDConnect();
            startRESTfulServer();
        }
    }

    /**
     * DConnectManagerを停止する.
     */
    public synchronized void stopInternal() {
        if (mRunningFlag) {
            mRunningFlag = false;
            stopRESTfulServer();
            stopDConnect();
        }
    }

    /**
     * WakeLockを登録にする.
     * <p>
     * {@link DConnectSettings#enableWakLock()}が{@code false}で、
     * {@link #mWakeLock}が{@code null}の場合のみ新しいWakeLocをします。
     * </p>
     */
    public void acquireWakeLock() {
        if (mWakeLock == null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("DConnectService acquire WakeLock.");
            }
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKE_LOCK);
            mWakeLock.acquire();
        }
    }

    /**
     * WakeLockを解除する.
     */
    public void releaseWakeLock() {
        if (mWakeLock != null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("DConnectService release WakeLock.");
            }
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * RESTfulサーバが動作しているかを確認する.
     * @return 動作している場合にはtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return mRunningFlag;
    }

    /**
     * DConnectServiceとバインドするためのクラス.
     */
    public class LocalBinder extends Binder {
        /**
         * DConnectServiceのインスタンスを取得する.
         *
         * @return DConnectServiceのインスタンス
         */
        public DConnectService getDConnectService() {
            return DConnectService.this;
        }
    }

    /**
     * ネットワークの接続状態の変化を受け取るレシーバー.
     */
    private final BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            showNotification();
        }
    };

    @Override
    protected String parseProfileName(final Intent request) {
        String profileName = super.parseProfileName(request);
        if (profileName != null) {
            //XXXX パスの大文字小文字を無視
            profileName = profileName.toLowerCase();
        }
        return profileName;
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile != null) {
            profile.setContext(this);
            //XXXX パスの大文字小文字を無視
            mProfileMap.put(profile.getProfileName().toLowerCase(), profile);
        }
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile != null) {
            //XXXX パスの大文字小文字を無視
            mProfileMap.remove(profile.getProfileName().toLowerCase());
        }
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        //XXXX パスの大文字小文字を無視
        return mProfileMap.get(name.toLowerCase());
    }

    @Override
    protected void sendDeliveryProfile(final Intent request, final Intent response) {
        //XXXX パスの互換性を担保
        for (MessageConverter converter : mRequestConverters) {
            converter.convert(request);
        }
        super.sendDeliveryProfile(request, response);
    }

    @Override
    protected Intent createResponseIntent(final Intent request, final Intent response) {
        Intent result = super.createResponseIntent(request, response);

        //XXXX パスの互換性の担保
        for (MessageConverter converter : mResponseConverters) {
            converter.convert(result);
        }
        return result;
    }

    public WebSocketInfoManager getWebSocketInfoManager() {
        return mWebSocketInfoManager;
    }
}
