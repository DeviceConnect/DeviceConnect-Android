/*
 DConnectMessageService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.manager.DConnectLocalOAuth.OAuthData;
import org.deviceconnect.android.manager.DevicePluginManager.DevicePluginEventListener;
import org.deviceconnect.android.manager.hmac.HmacManager;
import org.deviceconnect.android.manager.policy.OriginValidator;
import org.deviceconnect.android.manager.profile.AuthorizationProfile;
import org.deviceconnect.android.manager.profile.DConnectAvailabilityProfile;
import org.deviceconnect.android.manager.profile.DConnectDeliveryProfile;
import org.deviceconnect.android.manager.profile.DConnectFilesProfile;
import org.deviceconnect.android.manager.profile.DConnectServiceDiscoveryProfile;
import org.deviceconnect.android.manager.profile.DConnectSystemProfile;
import org.deviceconnect.android.manager.request.DConnectRequest;
import org.deviceconnect.android.manager.request.DConnectRequestManager;
import org.deviceconnect.android.manager.request.DiscoveryDeviceRequest;
import org.deviceconnect.android.manager.request.RegisterNetworkServiceDiscovery;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * DConnectMessageを受信するサービス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectMessageService extends Service 
        implements DConnectProfileProvider, DevicePluginEventListener {
    /** ドメイン名. */
    private static final String DCONNECT_DOMAIN = ".deviceconnect.org";
    /** ローカルのドメイン名. */
    private static final String LOCALHOST_DCONNECT = "localhost" + DCONNECT_DOMAIN;

    /** Notification ID.*/
    private static final int ONGOING_NOTIFICATION_ID = 4035;

    /** サービスIDやセッションキーを分割するセパレータ. */
    public static final String SEPARATOR = ".";

    /** セッションキーとreceiverを分けるセパレータ. */
    public static final String SEPARATOR_SESSION = "@";

    /** リクエストコードのエラー値を定義. */
    private static final int ERROR_CODE = Integer.MIN_VALUE;

    /** 起動用URIスキーム名. */
    private static final String SCHEME_LAUNCH = "dconnect";

    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** プロファイルインスタンスマップ. */
    protected Map<String, DConnectProfile> mProfileMap = new HashMap<String, DConnectProfile>();

    /** 最後に処理されるプロファイル. */
    private DConnectProfile mDeliveryProfile;

    /** Origin妥当性確認クラス. */
    private OriginValidator mOriginValidator;

    /** リクエスト管理クラス. */
    protected DConnectRequestManager mRequestManager;

    /** デバイスプラグイン管理. */
    protected DevicePluginManager mPluginMgr;

    /** DeviceConnectの設定. */
    protected DConnectSettings mSettings;

    /** ファイル管理用プロバイダ. */
    protected FileManager mFileMgr;

    /** Local OAuthのデータを管理するクラス. */
    private DConnectLocalOAuth mLocalOAuth;

    /** HMAC管理クラス. */
    private HmacManager mHmacManager;

    /** サーバの起動状態. */
    protected boolean mRunningFlag;

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("dconnect.manager");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }

        // イベント管理クラスの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // Local OAuthの初期化
        LocalOAuth2Main.initialize(getApplicationContext());

        // DConnect設定
        mSettings = DConnectSettings.getInstance();
        mSettings.load(this);

        // ファイル管理クラス
        mFileMgr = new FileManager(this);

        // デバイスプラグインとのLocal OAuth情報
        mLocalOAuth = new DConnectLocalOAuth(this);

        // デバイスプラグイン管理クラスの作成
        mPluginMgr = new DevicePluginManager(this, LOCALHOST_DCONNECT);
        mPluginMgr.setEventListener(this);

        // プロファイルの追加
        addProfile(new AuthorizationProfile());
        addProfile(new DConnectAvailabilityProfile());
        addProfile(new DConnectServiceDiscoveryProfile(null, mPluginMgr));
        addProfile(new DConnectFilesProfile(this));
        addProfile(new DConnectSystemProfile(this, mPluginMgr));

        // dConnect Managerで処理せず、登録されたデバイスプラグインに処理させるプロファイル
        setDeliveryProfile(new DConnectDeliveryProfile(mPluginMgr, mLocalOAuth,
                mSettings.requireOrigin()));
    }

    @Override
    public void onDestroy() {
        stopDConnect();
        LocalOAuth2Main.destroy();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (!mRunningFlag) {
            return START_STICKY;
        }

        if (intent == null) {
            mLogger.warning("intent is null.");
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            mLogger.warning("action is null.");
            return START_STICKY;
        }

        String scheme = intent.getScheme();
        if (SCHEME_LAUNCH.equals(scheme)) {
            String key = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
            String origin = intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            if (key != null && !TextUtils.isEmpty(origin)) {
                mHmacManager.updateKey(origin, key);
            }
            return START_STICKY;
        }

        if (checkAction(action)) {
            onRequestReceive(intent);
        } else if (IntentDConnectMessage.ACTION_RESPONSE.equals(action)) {
            onResponseReceive(intent);
        } else if (IntentDConnectMessage.ACTION_EVENT.equals(action)) {
            onEventReceive(intent);
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            mPluginMgr.checkAndAddDevicePlugin(intent);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            mPluginMgr.checkAndRemoveDevicePlugin(intent);
        }

        return START_STICKY;
    }

    /**
     * リクエスト用Intentを受領したときの処理を行う.
     * @param request リクエスト用Intent
     */
    public void onRequestReceive(final Intent request) {
        // リクエストコードが定義されていない場合には無視
        int requestCode = getRequestCode(request);
        if (requestCode == ERROR_CODE) {
            mLogger.warning("Illegal requestCode in onRequestReceive. requestCode=" + requestCode);
            return;
        }

        // 不要になったキャッシュファイルの削除を行う
        if (mFileMgr != null) {
            mFileMgr.checkAndRemove();
        }

        // レスポンス用のIntentの用意
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        response.putExtra(DConnectMessage.EXTRA_REQUEST_CODE, requestCode);

        // オリジンの正当性チェック
        String profileName = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        OriginValidator.OriginError error = mOriginValidator.checkOrigin(request);
        switch (error) {
        case NOT_SPECIFIED:
            MessageUtils.setInvalidOriginError(response, "Origin is not specified.");
            sendResponse(request, response);
            return;
        case NOT_UNIQUE:
            MessageUtils.setInvalidOriginError(response, "The specified origin is not unique.");
            sendResponse(request, response);
            return;
        case NOT_ALLOWED:
            // NOTE: Local OAuth関連のAPIに対する特別措置
            DConnectProfile profile = getProfile(profileName);
            if (profile != null && profile instanceof AuthorizationProfile) {
                ((AuthorizationProfile) profile).onInvalidOrigin(request, response);
            }
 
            MessageUtils.setInvalidOriginError(response, "The specified origin is not allowed.");
            sendResponse(request, response);
            return;
        case NONE:
        default:
            break;
        }

        if (profileName == null) {
            MessageUtils.setNotSupportProfileError(response);
            sendResponse(request, response);
            return;
        }

        if (mSettings.isUseALocalOAuth()) {
            // アクセストークンの取得
            String accessToken = request.getStringExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN);
            CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken,
                    profileName.toLowerCase(),
                    DConnectLocalOAuth.IGNORE_PROFILES);
            if (result.checkResult()) {
                executeRequest(request, response);
            } else {
                if (accessToken == null) {
                    MessageUtils.setEmptyAccessTokenError(response);
                } else if (!result.isExistAccessToken()) {
                    MessageUtils.setNotFoundClientId(response);
                } else if (!result.isExistClientId()) {
                    MessageUtils.setNotFoundClientId(response);
                } else if (!result.isExistScope()) {
                    MessageUtils.setScopeError(response);
                } else if (!result.isNotExpired()) {
                    MessageUtils.setExpiredAccessTokenError(response);
                } else {
                    MessageUtils.setAuthorizationError(response);
                }
                sendResponse(request, response);
            }
        } else {
            executeRequest(request, response);
        }
    }

    protected String parseProfileName(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
    }

    /**
     * レスポンス受信ハンドラー.
     * @param response レスポンス用Intent
     */
    private void onResponseReceive(final Intent response) {
        // リクエストコードが定義されていない場合にはエラー
        int requestCode = getRequestCode(response);
        if (requestCode == ERROR_CODE) {
            mLogger.warning("Illegal requestCode in onResponseReceive. requestCode=" + requestCode);
            return;
        }

        // レスポンスをリクエスト管理クラスに渡す
        mRequestManager.setResponse(response);
    }

    /**
     * イベントメッセージ受信ハンドラー.
     * @param event イベント用Intent
     */
    private void onEventReceive(final Intent event) {
        String sessionKey = event.getStringExtra(DConnectMessage.EXTRA_SESSION_KEY);
        String serviceId = event.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String profile = event.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String inter = event.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        String attribute = event.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("onEventReceive: [sessionKey: %s serviceId: %s profile: %s inter: %s attribute: %s]",
                sessionKey, serviceId, profile, inter, attribute));
        }

        if (sessionKey != null) {
            // セッションキーからreceiverを取得する
            String receiver = null;
            int index = sessionKey.indexOf(SEPARATOR_SESSION);
            if (index > 0) {
                receiver = sessionKey.substring(index + 1);
                sessionKey = sessionKey.substring(0, index);
            }
            // ここでセッションキーをデバイスプラグインIDを取得
            String pluginId = convertSessionKey2PluginId(sessionKey);
            String key = convertSessionKey2Key(sessionKey);
            DevicePlugin plugin = mPluginMgr.getDevicePlugin(pluginId);
            if (plugin == null) {
                mLogger.warning("plugin is null.");
                return;
            }
            event.putExtra(DConnectMessage.EXTRA_SESSION_KEY, key);

            // Local OAuthの仕様で、デバイスを発見するごとにclientIdを作成して、
            // アクセストークンを取得する作業を行う。
            if (ServiceDiscoveryProfileConstants.PROFILE_NAME.equals(profile) 
                || ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE.equals(attribute)) {

                // network service discoveryの場合には、networkServiceのオブジェクトの中にデータが含まれる
                Bundle service = event.getParcelableExtra(
                        ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE);
                String id = service.getString(ServiceDiscoveryProfile.PARAM_ID);
                String did = mPluginMgr.appendServiceId(plugin, id);

                // サービスIDを変更
                replaceServiceId(event, plugin);

                OAuthData oauth = mLocalOAuth.getOAuthData(did);
                if (oauth == null) {
                    createClientOfDevicePlugin(plugin, did, event);
                } else {
                    // 送信先のセッションを取得
                    List<Event> evts = EventManager.INSTANCE.getEventList(profile, attribute);
                    for (int i = 0; i < evts.size(); i++) {
                        Event evt = evts.get(i);
                        event.putExtra(DConnectMessage.EXTRA_SESSION_KEY, evt.getSessionKey());
                        sendEvent(evt.getReceiverName(), event);
                    }
                }
            } else {
                replaceServiceId(event, plugin);
                sendEvent(receiver, event);
            }
        } else {
            mLogger.warning("onEventReceive: sessionKey is null.");
        }
    }

    /**
     * リクエストを実行する.
     * @param request リクエスト
     * @param response レスポンス
     */
    private void executeRequest(final Intent request, final Intent response) {
        // リクエストにDeviceConnectManagerの情報を付加する
        request.putExtra(DConnectMessage.EXTRA_PRODUCT, getString(R.string.app_name));
        request.putExtra(DConnectMessage.EXTRA_VERSION, DConnectUtil.getVersionName(this));

        DConnectProfile profile = getProfile(request);
        if (profile != null && !isDeliveryRequest(request)) {
            if (profile.onRequest(request, response)) {
                sendResponse(request, response);
            }
        } else {
            sendDeliveryProfile(request, response);
        }
    }

    /**
     * 指定されたリクエストがデバイスプラグインに配送するリクエストか確認する.
     * @param request リクエスト
     * @return プラグインに配送する場合にはtrue、それ以外はfalse
     */
    private boolean isDeliveryRequest(final Intent request) {
        return DConnectSystemProfile.isWakeUpRequest(request);
    }

    /**
     * セッションキーからプラグインIDに変換する.
     *
     * @param sessionKey セッションキー
     * @return プラグインID
     */
    private String convertSessionKey2PluginId(final String sessionKey) {
        int index = sessionKey.lastIndexOf(SEPARATOR);
        if (index > 0) {
            return sessionKey.substring(index + 1);
        }
        return sessionKey;
    }

    /**
     * デバイスプラグインからのセッションキーから前半分のクライアントのセッションキーに変換する.
     * @param sessionKey セッションキー
     * @return クライアント用のセッションキー
     */
    private String convertSessionKey2Key(final String sessionKey) {
        int index = sessionKey.lastIndexOf(SEPARATOR);
        if (index > 0) {
            return sessionKey.substring(0, index);
        }
        return sessionKey;
    }

    /**
     * リクエストを追加する.
     * @param request 追加するリクエスト
     */
    public void addRequest(final DConnectRequest request) {
        mRequestManager.addRequest(request);
    }

    @Override
    public List<DConnectProfile> getProfileList() {
        return new ArrayList<>(mProfileMap.values());
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile != null) {
            profile.setContext(this);
            mProfileMap.put(profile.getProfileName(), profile);
        }
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile != null) {
            mProfileMap.remove(profile.getProfileName());
        }
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        return mProfileMap.get(name);
    }

    private DConnectProfile getProfile(final Intent request) {
        return getProfile(DConnectProfile.getProfile(request));
    }

    private int getRequestCode(final Intent response) {
        return response.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
    }

    /**
     * プロファイル処理が行われなかったときに呼び出されるプロファイルを設定する.
     * @param profile プロファイル
     */
    private void setDeliveryProfile(final DConnectProfile profile) {
        if (profile != null) {
            profile.setContext(this);
            mDeliveryProfile = profile;
        }
    }

    @Override
    public void onDeviceFound(final DevicePlugin plugin) {
        RegisterNetworkServiceDiscovery req = new RegisterNetworkServiceDiscovery();
        req.setContext(this);
        req.setSessionKey(plugin.getServiceId());
        req.setDestination(plugin);
        req.setDevicePluginManager(mPluginMgr);
        addRequest(req);
    }

    @Override
    public void onDeviceLost(final DevicePlugin plugin) {
        mLocalOAuth.deleteOAuthDatas(plugin.getServiceId());
    }

    /**
     * 指定されたアクションがdConnectのアクションをチェックする.
     * @param action アクション
     * @return dConnectのアクションの場合はtrue, それ以外はfalse
     */
    private boolean checkAction(final String action) {
        return (action.equals(IntentDConnectMessage.ACTION_GET) 
             || action.equals(IntentDConnectMessage.ACTION_PUT)
             || action.equals(IntentDConnectMessage.ACTION_POST) 
             || action.equals(IntentDConnectMessage.ACTION_DELETE));
    }

    /**
     * DConnectManagerを起動する。
     */
    protected synchronized void startDConnect() {
        // 設定の更新
        mSettings.load(this);

        if (BuildConfig.DEBUG) {
            mLogger.info("DConnectManager#Settings");
            mLogger.info("    SSL: " + mSettings.isSSL());
            mLogger.info("    Host: " + mSettings.getHost());
            mLogger.info("    Port: " + mSettings.getPort());
            mLogger.info("    Allow External IP: " + mSettings.allowExternalIP());
            mLogger.info("    RequireOrigin: " + mSettings.requireOrigin());
            mLogger.info("    LocalOAuth: " + mSettings.isUseALocalOAuth());
            mLogger.info("    OriginBlock: " + mSettings.isBlockingOrigin());
        }

        mHmacManager = new HmacManager(this);
        mRequestManager = new DConnectRequestManager();
        mOriginValidator = new OriginValidator(this,
                mSettings.requireOrigin(), mSettings.isBlockingOrigin());

        mPluginMgr.createDevicePluginList();

        showNotification();

        mRunningFlag = true;
    }

    /**
     * DConnectManagerを停止する.
     */
    protected synchronized void stopDConnect() {
        mRunningFlag = false;

        mRequestManager.shutdown();
        hideNotification();
    }

    /**
     * 各デバイスプラグインにリクエストを受け渡す.
     * 
     * ここで、アクセストークンをリクエストに付加する。
     * また、アクセストークンが存在しない場合には、デバイスプラグインにアクセストークンの取得要求を行う。
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void sendDeliveryProfile(final Intent request, final Intent response) {
        mDeliveryProfile.onRequest(request, response);
    }

    /**
     * イベント用メッセージのサービスIDを置換する.
     * <br>
     * 
     * デバイスプラグインから送られてくるサービスIDは、デバイスプラグインの中でIDになっている。
     * dConnect ManagerでデバイスプラグインのIDをサービスIDに付加することでDNSっぽい動きを実現する。
     *
     * @param event イベントメッセージ用Intent
     * @param plugin 送信元のデバイスプラグイン
     */
    private void replaceServiceId(final Intent event, final DevicePlugin plugin) {
        String serviceId = event.getStringExtra(IntentDConnectMessage.EXTRA_SERVICE_ID);
        event.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID,
            mPluginMgr.appendServiceId(plugin, serviceId));
    }

    /**
     * サービスをフォアグランドに設定する。
     */
    protected void showNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), SettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(DConnectUtil.getIPAddress(this) + ":" + mSettings.getPort());
        int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                R.drawable.icon : R.drawable.on_icon;
        builder.setSmallIcon(iconType);

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    /**
     * フォアグランドを停止する。
     */
    protected void hideNotification() {
        stopForeground(true);
    }

    /**
     * デバイスプラグインのクライアントを作成する.
     * @param plugin クライアントを作成するデバイスプラグイン
     * @param serviceId サービスID
     * @param event 送信するイベント
     */
    private void createClientOfDevicePlugin(final DevicePlugin plugin, final String serviceId, final Intent event) {
        Intent intent = new Intent(IntentDConnectMessage.ACTION_GET);
        intent.setComponent(plugin.getComponentName());
        intent.putExtra(DConnectMessage.EXTRA_PROFILE,
                ServiceDiscoveryProfileConstants.PROFILE_NAME);
        intent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);

        DiscoveryDeviceRequest request = new DiscoveryDeviceRequest();
        request.setContext(this);
        request.setLocalOAuth(mLocalOAuth);
        request.setUseAccessToken(true);
        request.setRequireOrigin(true);
        request.setDestination(plugin);
        request.setRequest(intent);
        request.setEvent(event);
        request.setDevicePluginManager(mPluginMgr);
        addRequest(request);
    }

    /**
     * レスポンス用のIntentを作成する.
     * @param request リクエスト
     * @param response リクエストに対応するレスポンス
     * @return 送信するレスポンス用Intent
     */
    protected Intent createResponseIntent(final Intent request, final Intent response) {
        int requestCode = request.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
        ComponentName cn = request
                .getParcelableExtra(IntentDConnectMessage.EXTRA_RECEIVER);

        Intent intent = new Intent(response);
        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(IntentDConnectMessage.EXTRA_PRODUCT, getString(R.string.app_name));
        intent.putExtra(IntentDConnectMessage.EXTRA_VERSION, DConnectUtil.getVersionName(this));

        // HMAC生成
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (origin == null) {
            String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
            if (accessToken != null) {
                origin = findOrigin(accessToken);
            }
        }
        if (origin != null) {
            if (mHmacManager.usesHmac(origin)) {
                String nonce = request.getStringExtra(IntentDConnectMessage.EXTRA_NONCE);
                if (nonce != null) {
                    String hmac = mHmacManager.generateHmac(origin, nonce);
                    if (hmac != null) {
                        intent.putExtra(IntentDConnectMessage.EXTRA_HMAC, hmac);
                    }
                }
            }
        } else {
            mLogger.warning("Origin is not found.");
        }

        intent.setComponent(cn);
        return intent;
    }

    /**
     * 指定されたアクセストークンのOriginを取得する.
     * 
     * @param accessToken アクセストークン
     * @return Origin
     */
    private String findOrigin(final String accessToken) {
        ClientPackageInfo packageInfo = LocalOAuth2Main.findClientPackageInfoByAccessToken(accessToken);
        if (packageInfo == null) {
            return null;
        }
        // Origin is a package name of LocalOAuth client.
        return packageInfo.getPackageInfo().getPackageName();
    }

    /**
     * リクエストの送信元にレスポンスを返却する.
     * @param request 送信元のリクエスト
     * @param response 返却するレスポンス
     */
    public void sendResponse(final Intent request, final Intent response) {
        sendBroadcast(createResponseIntent(request, response));
    }

    /**
     * イベントメッセージを送信する.
     * @param receiver 送信先のBroadcastReceiver
     * @param event 送信するイベントメッセージ
     */
    public void sendEvent(final String receiver, final Intent event) {
        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("sendEvent: %s intent: %s", receiver, event.getExtras()));
        }
        Intent targetIntent = new Intent(event);
        targetIntent.setComponent(ComponentName.unflattenFromString(receiver));
        sendBroadcast(targetIntent);
    }
}
