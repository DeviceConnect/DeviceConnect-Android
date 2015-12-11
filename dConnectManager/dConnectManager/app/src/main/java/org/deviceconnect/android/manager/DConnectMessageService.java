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
import org.deviceconnect.android.manager.policy.OriginParser;
import org.deviceconnect.android.manager.policy.Whitelist;
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
    /** fileスキームのオリジン. */
    private static final String ORIGIN_FILE = "file://";
    /** 常に許可するオリジン一覧. */
    private static final String[] IGNORED_ORIGINS = {ORIGIN_FILE};

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

    /** dConnect Managerのドメイン名. */
    private String mDConnectDomain = LOCALHOST_DCONNECT;

    /** プロファイルインスタンスマップ. */
    private Map<String, DConnectProfile> mProfileMap = new HashMap<String, DConnectProfile>();

    /** 最後に処理されるプロファイル. */
    private DConnectProfile mDeliveryProfile;

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

    /** ホワイトリスト管理クラス. */
    private Whitelist mWhitelist;

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
        mPluginMgr = new DevicePluginManager(this, mDConnectDomain);
        mPluginMgr.setEventListener(this);

        // プロファイルの追加
        addProfile(new AuthorizationProfile());
        addProfile(new DConnectAvailabilityProfile());
        addProfile(new DConnectServiceDiscoveryProfile(this, mPluginMgr));
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
        super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            mLogger.warning("intent is null.");
            return START_STICKY;
        }

        if (!mRunningFlag) {
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
        // リクエストコードが定義されていない場合にはエラー
        int requestCode = request.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
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
        OriginError error = checkOrigin(request);
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
            CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken, profileName,
                    DConnectLocalOAuth.IGNORE_PROFILE);
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

    /**
     * オリジンの正当性をチェックする.
     * <p>
     * 設定画面上でオリジン要求フラグがOFFにされた場合は即座に「エラー無し」を返す。
     * </p>
     * @param request 送信元のリクエスト
     * @return チェック処理の結果
     */
    private OriginError checkOrigin(final Intent request) {
        if (!mSettings.requireOrigin()) {
            return OriginError.NONE;
        }
        String originParam = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (originParam == null) {
            return OriginError.NOT_SPECIFIED;
        }
        String[] origins = originParam.split(" ");
        if (origins.length != 1) {
            return OriginError.NOT_UNIQUE;
        }
        if (!allowsOrigin(request)) {
            return OriginError.NOT_ALLOWED;
        }
        return OriginError.NONE;
    }

    /**
     * レスポンス受信ハンドラー.
     * @param response レスポンス用Intent
     */
    public void onResponseReceive(final Intent response) {
        // リクエストコードが定義されていない場合にはエラー
        int requestCode = response.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
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
    public void onEventReceive(final Intent event) {
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
            String did = mPluginMgr.appendServiceId(plugin, serviceId);
            event.putExtra(DConnectMessage.EXTRA_SESSION_KEY, key);

            // Local OAuthの仕様で、デバイスを発見するごとにclientIdを作成して、
            // アクセストークンを取得する作業を行う。
            if (ServiceDiscoveryProfileConstants.PROFILE_NAME.equals(profile) 
                || ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE.equals(attribute)) {

                // network service discoveryの場合には、networkServiceのオブジェクトの中にデータが含まれる
                Bundle service = (Bundle) event.getParcelableExtra(
                        ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE);
                String id = service.getString(ServiceDiscoveryProfile.PARAM_ID);
                did = mPluginMgr.appendServiceId(plugin, id);

                // サービスIDを変更
                replaceServiceId(event, plugin);

                OAuthData oauth = mLocalOAuth.getOAuthData(did);
                if (oauth == null && plugin != null) {
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

        boolean send = false;
        String profileName = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        DConnectProfile profile = getProfile(profileName);
        if (profile != null) {
            send = profile.onRequest(request, response);
        }
        if (!send) {
            sendDeliveryProfile(request, response);
        }
    }

    /**
     * セッションキーからプラグインIDに変換する.
     * 
     * 
     * @param sessionkey セッションキー
     * @return プラグインID
     */
    private String convertSessionKey2PluginId(final String sessionkey) {
        int index = sessionkey.lastIndexOf(SEPARATOR);
        if (index > 0) {
            return sessionkey.substring(index + 1);
        }
        return sessionkey;
    }

    /**
     * デバイスプラグインからのセッションキーから前半分のクライアントのセッションキーに変換する.
     * @param sessionkey セッションキー
     * @return クライアント用のセッションキー
     */
    private String convertSessionKey2Key(final String sessionkey) {
        int index = sessionkey.lastIndexOf(SEPARATOR);
        if (index > 0) {
            return sessionkey.substring(0, index);
        }
        return sessionkey;
    }

    /**
     * リクエストを追加する.
     * @param request 追加するリクエスト
     */
    public void addRequest(final DConnectRequest request) {
        mRequestManager.addRequest(request);
    }

    /**
     * DConnectLocalOAuthのインスタンスを取得する.
     * @return DConnectLocalOAuthのインスタンス
     */
    public DConnectLocalOAuth getLocalOAuth() {
        return mLocalOAuth;
    }

    @Override
    public List<DConnectProfile> getProfileList() {
        List<DConnectProfile> profileList = new ArrayList<DConnectProfile>(mProfileMap.values());
        return profileList;
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

    /**
     * プロファイル処理が行われなかったときに呼び出されるプロファイルを設定する.
     * @param profile プロファイル
     */
    public void setDeliveryProfile(final DConnectProfile profile) {
        if (profile != null) {
            profile.setContext(this);
            mDeliveryProfile = profile;
        }
    }

    /**
     * 指定したプロファイル名のDConnectProfileを取得する.
     * 指定したプロファイル名のDConnectProfileが存在しない場合にはnullを返却する。
     * @param name プロファイル名
     * @return DConnectProfileのインスタンス
     */
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        return mProfileMap.get(name);
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
        mRunningFlag = true;

        // 設定の更新
        mSettings.load(this);

        if (BuildConfig.DEBUG) {
            mLogger.info("Settings");
            mLogger.info("    SSL: " + mSettings.isSSL());
            mLogger.info("    Host: " + mSettings.getHost());
            mLogger.info("    Port: " + mSettings.getPort());
            mLogger.info("    LocalOAuth: " + mSettings.isUseALocalOAuth());
            mLogger.info("    OriginBlock: " + mSettings.isBlockingOrigin());
        }

        // HMAC管理クラス
        mHmacManager = new HmacManager(this);
        // ホワイトリスト管理クラス
        mWhitelist = new Whitelist(this);
        // リクエスト管理クラスの作成
        mRequestManager = new DConnectRequestManager();
        // デバイスプラグインの更新
        mPluginMgr.createDevicePluginList();
        showNotification();
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
    private void sendDeliveryProfile(final Intent request, final Intent response) {
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
        String serviceId = event
                .getStringExtra(IntentDConnectMessage.EXTRA_SERVICE_ID);
        event.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID,
                mPluginMgr.appendServiceId(plugin, serviceId));
    }

    /**
     * サービスをフォアグランドに設定する。
     */
    private void showNotification() {
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
    private void hideNotification() {
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
     * 指定されたリクエストのオリジンが許可されるかどうかを返す.
     * 
     * @param request 受信したリクエスト
     * @return 指定されたリクエストのオリジンが許可される場合は<code>true</code>、
     *      そうでない場合は<code>false</code>
     */
    private boolean allowsOrigin(final Intent request) {
        String originExp = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (originExp == null) {
            // NOTE: クライアント作成のためにオリジンが必要のため、
            // ホワイトリストが無効の場合でもオリジン指定のない場合はリクエストを許可しない.
            return false;
        }
        for (int i = 0; i < IGNORED_ORIGINS.length; i++) {
            if (originExp.equals(IGNORED_ORIGINS[i])) {
                return true;
            }
        }
        if (!mSettings.isBlockingOrigin()) {
            return true;
        }
        return mWhitelist.allows(OriginParser.parse(originExp));
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

    /**
     * Originヘッダ解析時に検出したエラー.
     */
    private enum OriginError {
        /**
         * エラー無しを示す定数.
         */
        NONE,

        /**
         * オリジンが指定されていないことを示す定数.
         */
        NOT_SPECIFIED, 

        /**
         * 2つ以上のオリジンが指定されていたことを示す定数.
         */
        NOT_UNIQUE,

        /**
         * 指定されたオリジンが許可されていない(ホワイトリストに含まれていない)ことを示す定数.
         */
        NOT_ALLOWED
    }
}
