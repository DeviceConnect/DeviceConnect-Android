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
import android.os.IBinder;
import android.os.RemoteException;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.compat.CompatibleRequestConverter;
import org.deviceconnect.android.manager.compat.ServiceDiscoveryConverter;
import org.deviceconnect.android.manager.compat.ServiceInformationConverter;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.event.KeepAlive;
import org.deviceconnect.android.manager.event.KeepAliveManager;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * dConnect Manager本体.
 * @author NTT DOCOMO, INC.
 */
public class DConnectService extends DConnectMessageService {
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

    /** イベント送信スレッド. */
    private ExecutorService mEventSender = Executors.newSingleThreadExecutor();

    /** イベントKeep Alive管理クラス. */
    private KeepAliveManager mKeepAliveManager;

    private MessageConverter[] mRequestConverters;
    private MessageConverter[] mResponseConverters;

    @Override
    public IBinder onBind(final Intent intent) {
        return (IBinder) mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
                new CompatibleRequestConverter(mPluginMgr)
        };
        mResponseConverters = new MessageConverter[] {
                new ServiceDiscoveryConverter(),
                new ServiceInformationConverter()
        };
    }

    @Override
    public void onDestroy() {
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
            if (webSocketId != null) {
                mRESTfulServer.disconnectWebSocket(webSocketId);
            }
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
                sendDisconnectWebSocket(receiverId);
            }
        }
    }

    private boolean isSupportedKeepAlive(final DevicePlugin plugin) {
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
            final String key = event.getStringExtra(IntentDConnectMessage.EXTRA_SESSION_KEY);
            mEventSender.execute(new Runnable() {
                @Override
                public void run() {
                    if (key != null && mRESTfulServer != null && mRESTfulServer.isRunning()) {
                        WebSocketInfo info = getWebSocketInfo(key);
                        if (info == null) {
                            mLogger.warning("sendEvent: webSocket is not found: key = " + key);
                            return;
                        }

                        try {
                            if (BuildConfig.DEBUG) {
                                mLogger.info(String.format("sendEvent: %s extra: %s", key, event.getExtras()));
                            }
                            JSONObject root = new JSONObject();
                            DConnectUtil.convertBundleToJSON(root, event.getExtras());

                            mRESTfulServer.sendEvent(info.getRawId(), root.toString());
                        } catch (JSONException e) {
                            mLogger.warning("JSONException in sendEvent: " + e.toString());
                        } catch (IOException e) {
                            mLogger.warning("IOException in sendEvent: " + e.toString());
                            if (mWebServerListener != null) {
                                mWebServerListener.onWebSocketDisconnected(info.getRawId());
                            }
                        }
                    }
                }
            });
        } else {
            super.sendEvent(receiver, event);
        }
    }

    /**
     * 該当セッションキーを持つWebSocket切断要求を送る.
     * @param receiverId イベントレシーバーID.
     */
    public void sendDisconnectWebSocket(final String receiverId) {
        if (receiverId != null) {
            mEventSender.execute(new Runnable() {
                @Override
                public void run() {
                    WebSocketInfo info = getWebSocketInfo(receiverId);
                    if (info != null) {
                        mRESTfulServer.disconnectWebSocket(info.getRawId());
                    } else {
                        mLogger.warning("sendDisconnectWebSocket: WebSocketInfo is not found: key = " + receiverId);
                    }
                }
            });
        }
    }

    private WebSocketInfo getWebSocketInfo(final String receiverId) {
        return ((DConnectApplication) getApplication()).getWebSocketInfoManager().getWebSocketInfo(receiverId);
    }

    /**
     * HTTPサーバを開始する.
     */
    private void startRESTfulServer() {
        mEventSender.execute(new Runnable() {
            @Override
            public void run() {
                mSettings.load(getApplicationContext());

                mWebServerListener = new DConnectServerEventListenerImpl(DConnectService.this);
                mWebServerListener.setFileManager(mFileMgr);

                DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
                builder.port(mSettings.getPort()).isSsl(mSettings.isSSL())
                        .documentRootPath(getFilesDir().getAbsolutePath());

                if (!mSettings.allowExternalIP()) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("127.0.0.1");
                    list.add("::1");
                    builder.ipWhiteList(list);
                }

                if (BuildConfig.DEBUG) {
                    mLogger.info("RESTful Server was Started.");
                    mLogger.info("Host: " + mSettings.getHost());
                    mLogger.info("Port: " + mSettings.getPort());
                    mLogger.info("SSL: " + mSettings.isSSL());
                    mLogger.info("External IP: " + mSettings.allowExternalIP());
                }

                if (mRESTfulServer == null) {
                    mRESTfulServer = new DConnectServerNanoHttpd(builder.build(), getApplicationContext());
                    mRESTfulServer.setServerEventListener(mWebServerListener);
                    mRESTfulServer.start();

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    registerReceiver(mWiFiReceiver, filter);
                }
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
    private void startInternal() {
        if (!mRunningFlag) {
            mRunningFlag = true;
            startDConnect();
            startRESTfulServer();
        }
    }

    /**
     * DConnectManagerを停止する.
     */
    private void stopInternal() {
        if (mRunningFlag) {
            mRunningFlag = false;
            stopRESTfulServer();
            stopDConnect();
        }
    }

    /**
     * バインドするためのスタブクラス.
     */
    private final IDConnectService mBinder = new IDConnectService.Stub()  {
        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return mRunningFlag;
        }

        @Override
        public void start() throws RemoteException {
            startInternal();
        }

        @Override
        public void stop() throws RemoteException {
            stopInternal();
        }
    };

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
}
