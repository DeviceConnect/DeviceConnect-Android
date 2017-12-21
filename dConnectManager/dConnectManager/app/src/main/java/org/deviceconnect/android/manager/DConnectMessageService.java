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
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.event.EventSessionTable;
import org.deviceconnect.android.manager.hmac.HmacManager;
import org.deviceconnect.android.manager.plugin.BinderConnection;
import org.deviceconnect.android.manager.plugin.BroadcastConnection;
import org.deviceconnect.android.manager.plugin.Connection;
import org.deviceconnect.android.manager.plugin.ConnectionFactory;
import org.deviceconnect.android.manager.plugin.ConnectionState;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.DevicePluginManager.DevicePluginEventListener;
import org.deviceconnect.android.manager.plugin.InternalConnection;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.android.manager.plugin.PluginDetectionException;
import org.deviceconnect.android.manager.policy.OriginValidator;
import org.deviceconnect.android.manager.profile.AuthorizationProfile;
import org.deviceconnect.android.manager.profile.DConnectAvailabilityProfile;
import org.deviceconnect.android.manager.profile.DConnectDeliveryProfile;
import org.deviceconnect.android.manager.profile.DConnectServiceDiscoveryProfile;
import org.deviceconnect.android.manager.profile.DConnectSystemProfile;
import org.deviceconnect.android.manager.request.DConnectRequest;
import org.deviceconnect.android.manager.request.DConnectRequestManager;
import org.deviceconnect.android.manager.request.RegisterNetworkServiceDiscovery;
import org.deviceconnect.android.manager.setting.ErrorDialogActivity;
import org.deviceconnect.android.manager.setting.ErrorDialogFragment;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParser;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParserFactory;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * DConnectMessageを受信するサービス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectMessageService extends Service
        implements DConnectProfileProvider, DevicePluginEventListener {

    /** アクション名: プラグイン有効化要求. */
    public static final String ACTION_ENABLE_PLUGIN = "org.deviceconnect.android.action.ENABLE_PLUGIN";

    /** アクション名: プラグイン無効化要求. */
    public static final String ACTION_DISABLE_PLUGIN = "org.deviceconnect.android.action.DISABLE_PLUGIN";

    /** アクション名: プラグイン設定画面起動要求. */
    public static final String ACTION_OPEN_SETTINGS = "org.deviceconnect.android.action.OPEN_SETTINGS";

    /** エクストラ名: プラグインID. */
    public static final String EXTRA_PLUGIN_ID = "pluginId";

    /** アクション名: パッケージインストール通知. */
    public static final String ACTION_PACKAGE_ADDED = "org.deviceconnect.android.action.PACKAGE_ADDED";

    /** アクション名: パッケージアンインストール通知. */
    public static final String ACTION_PACKAGE_REMOVED = "org.deviceconnect.android.action.PACKAGE_REMOVED";

    /** エクストラ名: インストールまたはアンインストールされたパッケージの名前. */
    public static final String EXTRA_PACKAGE_NAME = "packageName";

    /** 匿名オリジン. */
    public static final String ANONYMOUS_ORIGIN = "<anonymous>";

    /** ドメイン名. */
    private static final String DCONNECT_DOMAIN = ".deviceconnect.org";

    /** ローカルのドメイン名. */
    private static final String LOCALHOST_DCONNECT = "localhost" + DCONNECT_DOMAIN;

    /** Notification ID.*/
    private static final int ONGOING_NOTIFICATION_ID = 4035;

    /** プロファイル仕様定義ファイルの拡張子. */
    private static final String SPEC_FILE_EXTENSION = ".json";

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

    /** デバイスプラグイン管理クラス. */
    private DevicePluginManager mPluginManager;

    /** 最後に処理されるプロファイル. */
    private DConnectProfile mDeliveryProfile;

    /** Origin妥当性確認クラス. */
    private OriginValidator mOriginValidator;

    /** リクエスト管理クラス. */
    protected DConnectRequestManager mRequestManager;

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

    /** イベントセッション管理テーブル. */
    protected final EventSessionTable mEventSessionTable = new EventSessionTable();

    /** イベントブローカー. */
    protected EventBroker mEventBroker;

    /** スレッドプール. */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(10);

    /** プラグイン検索中フラグ. */
    private boolean mIsSearchingPlugins;

    /** プロダクト名. */
    private String mProductName;

    /** バージョン名. */
    private String mVersionName;

    private IDConnectCallback mCallback = new IDConnectCallback.Stub() {
        @Override
        public void sendMessage(final Intent message) throws RemoteException {
            if (BuildConfig.DEBUG) {
                mLogger.info("IDConnectCallback.sendMessage: from = " + getCallingPackage());
            }
            handleExternalMessage(message);
        }
    };

    private String getCallingPackage() {
        int uid = Binder.getCallingUid();
        return getPackageManager().getNameForUid(uid);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mProductName = getString(R.string.app_name);
        mVersionName = DConnectUtil.getVersionName(this);

        // プラグイン管理クラスの初期化
        mPluginManager = new DevicePluginManager(this, LOCALHOST_DCONNECT);
        mPluginManager.addEventListener(this);
        mPluginManager.setConnectionFactory(new ConnectionFactory() {
            @Override
            public Connection createConnectionForPlugin(final DevicePlugin plugin) {
                Context context = DConnectMessageService.this;
                switch (plugin.getConnectionType()) {
                    case INTERNAL:
                        return new InternalConnection(context, plugin.getPluginId(), plugin.getComponentName(), mCallback);
                    case BINDER:
                        return new BinderConnection(context, plugin.getPluginId(), plugin.getComponentName(), mCallback);
                    case BROADCAST:
                        return new BroadcastConnection(context, plugin.getPluginId());
                    default:
                        return null;
                }
            }
        });

        // イベント管理クラスの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // Local OAuthの初期化
        LocalOAuth2Main.initialize(getApplicationContext());

        // DConnect設定
        mSettings = ((DConnectApplication) getApplication()).getSettings();

        // ファイル管理クラス
        mFileMgr = new FileManager(this);

        // デバイスプラグインとのLocal OAuth情報
        mLocalOAuth = new DConnectLocalOAuth(this);

        // イベントハンドラーの初期化
        mEventBroker = new EventBroker(this, mEventSessionTable, mLocalOAuth, mPluginManager);

        // プロファイルの追加
        addProfile(new AuthorizationProfile());
        addProfile(new DConnectAvailabilityProfile());
        addProfile(new DConnectServiceDiscoveryProfile(null, mPluginManager));
        addProfile(new DConnectSystemProfile(this, mPluginManager));

        // dConnect Managerで処理せず、登録されたデバイスプラグインに処理させるプロファイル
        setDeliveryProfile(new DConnectDeliveryProfile(mPluginManager, mLocalOAuth,
            mEventBroker, mSettings.requireOrigin()));

        loadProfileSpecs();
        startPluginSearch();
    }

    @Override
    public void onDestroy() {
        mPluginManager.removeEventListener(this);
        stopDConnect();
        LocalOAuth2Main.destroy();
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

        if (handleInternalMessage(intent)) {
            return START_NOT_STICKY;
        }

        if (!mRunningFlag) {
            return START_NOT_STICKY;
        }

        handleExternalMessage(intent);
        return START_STICKY;
    }

    private boolean handleInternalMessage(final Intent intent) {
        String action = intent.getAction();
        if (ACTION_ENABLE_PLUGIN.equals(action)) {
            final DevicePlugin plugin = findPlugin(intent);
            if (plugin != null) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        plugin.enable();
                    }
                });
            }
            return true;
        } else if (ACTION_DISABLE_PLUGIN.equals(action)) {
            final DevicePlugin plugin = findPlugin(intent);
            if (plugin != null) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        plugin.disable();
                    }
                });
            }
            return true;
        } else if (ACTION_OPEN_SETTINGS.equals(action)) {
            openSettings(intent);
            return true;
        }
        return false;
    }

    private void handleExternalMessage(final Intent intent) {
        String action = intent.getAction();
        String scheme = intent.getScheme();
        if (SCHEME_LAUNCH.equals(scheme)) {
            String key = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
            String origin = intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            if (key != null && !TextUtils.isEmpty(origin)) {
                mHmacManager.updateKey(origin, key);
            }
        } else if (checkAction(action)) {
            onRequestReceive(intent);
        } else if (IntentDConnectMessage.ACTION_RESPONSE.equals(action)) {
            onResponseReceive(intent);
        } else if (IntentDConnectMessage.ACTION_EVENT.equals(action)) {
            onEventReceive(intent);
        } else if (ACTION_PACKAGE_ADDED.equals(action)) {
            String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            mPluginManager.checkAndAddDevicePlugin(packageName);
        } else if (ACTION_PACKAGE_REMOVED.equals(action)) {
            String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            mPluginManager.checkAndRemoveDevicePlugin(packageName);
        }
    }

    private DevicePlugin findPlugin(final Intent intent) {
        String pluginId = intent.getStringExtra(EXTRA_PLUGIN_ID);
        if (pluginId == null) {
            return null;
        }
        return mPluginManager.getDevicePlugin(pluginId);
    }

    private void openSettings(final Intent intent) {
        DevicePlugin plugin = findPlugin(intent);
        if (plugin == null) {
            return;
        }

        Intent request = new Intent();
        request.setComponent(plugin.getComponentName());
        request.setAction(IntentDConnectMessage.ACTION_PUT);
        SystemProfile.setApi(request, "gotapi");
        SystemProfile.setProfile(request, SystemProfile.PROFILE_NAME);
        SystemProfile.setInterface(request, SystemProfile.INTERFACE_DEVICE);
        SystemProfile.setAttribute(request, SystemProfile.ATTRIBUTE_WAKEUP);
        request.putExtra("pluginId", plugin.getPluginId());
        sendMessage(plugin, request);
    }

    /**
     * リクエスト用Intentを受領したときの処理を行う.
     * @param request リクエスト用Intent
     */
    private void onRequestReceive(final Intent request) {
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
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        OriginValidator.OriginError error = mOriginValidator.checkOrigin(origin);
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
                if (origin == null && !mSettings.requireOrigin()) {
                    request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, ANONYMOUS_ORIGIN);
                }
                break;
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
        if (mRequestManager != null) {
            mRequestManager.setResponse(response);
        }
    }

    /**
     * イベントメッセージ受信ハンドラー.
     * @param event イベント用Intent
     */
    private void onEventReceive(final Intent event) {
        mEventBroker.onEvent(event);
    }

    /**
     * リクエストを実行する.
     * @param request リクエスト
     * @param response レスポンス
     */
    private void executeRequest(final Intent request, final Intent response) {
        // リクエストにDeviceConnectManagerの情報を付加する
        request.putExtra(DConnectMessage.EXTRA_PRODUCT, mProductName);
        request.putExtra(DConnectMessage.EXTRA_VERSION, mVersionName);

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
     * リクエストを追加する.
     * @param request 追加するリクエスト
     */
    public void addRequest(final DConnectRequest request) {
        if (mRequestManager != null) {
            mRequestManager.addRequest(request);
        }
    }

    private void loadProfileSpecs() {
        for (DConnectProfile profile : mProfileMap.values()) {
            final String profileName = profile.getProfileName();
            try {
                profile.setProfileSpec(loadProfileSpec(profileName));
                mLogger.info("Loaded a profile spec: " + profileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            } catch (JSONException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            }
        }
    }

    private DConnectProfileSpec loadProfileSpec(final String profileName)
            throws IOException, JSONException {
        AssetManager assets = getAssets();
        String path = findProfileSpecPath(assets, profileName);
        if (path == null) {
            return null;
        }
        String json = loadFile(assets.open(path));
        DConnectProfileSpecJsonParser parser =
                DConnectProfileSpecJsonParserFactory.getDefaultFactory().createParser();
        return parser.parseJson(new JSONObject(json));
    }

    private static String findProfileSpecPath(final AssetManager assets, final String profileName)
            throws IOException {
        String[] fileNames = assets.list("api");
        if (fileNames == null) {
            return null;
        }
        for (String fileFullName : fileNames) {
            if (!fileFullName.endsWith(SPEC_FILE_EXTENSION)) {
                continue;
            }
            String fileName = fileFullName.substring(0,
                    fileFullName.length() - SPEC_FILE_EXTENSION.length());
            if (fileName.equalsIgnoreCase(profileName)) {
                return "api/" + fileFullName;
            }
        }
        throw new FileNotFoundException("A spec file is not found: " + profileName);
    }

    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            return new String(baos.toByteArray());
        } finally {
            in.close();
        }
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

    /**
     * リクエスト用Intentからプロファイルを取得する.
     * @param request リクエスト用Intent
     * @return プロファイル
     */
    private DConnectProfile getProfile(final Intent request) {
        return getProfile(DConnectProfile.getProfile(request));
    }

    /**
     * リクエスト用Intentからプロファイル名を取得する.
     * @param request リクエスト用Intent
     * @return プロファイル名
     */
    protected String parseProfileName(final Intent request) {
        return DConnectProfile.getProfile(request);
    }

    /**
     * レスポンス用Intentからリクエストコードを取得する.
     * @param response レスポンス用Intent
     * @return リクエストコード
     */
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
        plugin.apply();
        if (plugin.isEnabled()) {
            RegisterNetworkServiceDiscovery req = new RegisterNetworkServiceDiscovery();
            req.setContext(this);
            req.setDestination(plugin);
            req.setDevicePluginManager(mPluginManager);
            addRequest(req);
        }
    }

    @Override
    public void onDeviceLost(final DevicePlugin plugin) {
        mEventBroker.removeSessionForPlugin(plugin.getPluginId());
        mLocalOAuth.deleteOAuthDatas(plugin.getPluginId());
    }

    @Override
    public void onConnectionStateChanged(final DevicePlugin plugin, final ConnectionState state) {
        // NOP.
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
    protected void startDConnect() {
        mHmacManager = new HmacManager(this);
        mRequestManager = new DConnectRequestManager();
        mOriginValidator = new OriginValidator(this,
                mSettings.requireOrigin(), mSettings.isBlockingOrigin());
        showNotification();
        sendLaunchedEvent();
    }

    private synchronized void startPluginSearch() {
        if (mIsSearchingPlugins) {
            return;
        }
        mIsSearchingPlugins = true;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mPluginManager.createDevicePluginList();
                } catch (PluginDetectionException e) {
                    showPluginListError(e);
                } finally {
                    mIsSearchingPlugins = false;
                }
            }
        });
    }

    private void showPluginListError(final PluginDetectionException e) {
        // エラーメッセージ初期化
        int messageId;
        switch (e.getReason()) {
            case TOO_MANY_PACKAGES:
                messageId = R.string.dconnect_error_plugin_not_detected_due_to_too_many_packages;
                break;
            default:
                messageId = R.string.dconnect_error_plugin_not_detected_due_to_unknown_error;
                break;
        }
        final String message = getString(messageId);
        final String title = getString(R.string.dconnect_error_plugin_not_detected_title);

        showErrorDialog(title, message);
    }

    private void showErrorDialog(final String title, final String message) {
        Intent intent = new Intent(this, ErrorDialogActivity.class);
        intent.putExtra(ErrorDialogFragment.EXTRA_TITLE, title);
        intent.putExtra(ErrorDialogFragment.EXTRA_MESSAGE, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * DConnectManagerを停止する.
     */
    protected void stopDConnect() {
        sendTerminatedEvent();
        if (mRequestManager != null) {
            mRequestManager.shutdown();
        }
        hideNotification();
    }

    private void sendMessage(final DevicePlugin plugin, final Intent message) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.send(message);
                } catch (MessagingException e) {
                    mLogger.warning("Failed to send event: action = " + message.getAction() + ", destination = " + plugin.getComponentName());
                }
            }
        });
    }

    /**
     * 全デバイスプラグインに対して、Device Connect Managerのライフサイクルについての通知を行う.
     */
    public void sendManagerEvent(final String action, final Bundle extras) {
        List<DevicePlugin> plugins = mPluginManager.getDevicePlugins();
        List<DevicePlugin> skipped = new ArrayList<>();
        for (DevicePlugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                skipped.add(plugin);
                continue;
            }
            if (plugin.getPluginId() != null) {
                Intent request = new Intent(action);
                request.setComponent(plugin.getComponentName());
                if (extras != null) {
                    request.putExtras(extras);
                }
                request.putExtra("pluginId", plugin.getPluginId());
                sendMessage(plugin, request);
            }
        }
        if (BuildConfig.DEBUG) {
            String message =  "Skipped sending " + action + ": " + skipped.size() + " plugin(s)";
            if (skipped.size() > 0) {
                message += " below\n" + listPluginsForLogger(skipped);
            }
            mLogger.info(message);
        }
    }

    private void sendManagerEvent(final String action) {
        sendManagerEvent(action, null);
    }

    private static String listPluginsForLogger(final List<DevicePlugin> plugins) {
        final String MARKER = " - ";
        StringBuilder exp = new StringBuilder("");
        for (DevicePlugin plugin : plugins) {
            exp.append(MARKER)
               .append(plugin.getDeviceName())
               .append(" (")
               .append(plugin.getComponentName())
               .append(")\n");
        }
        return exp.toString();
    }

    /**
     * 全デバイスプラグインに対して、Device Connect Manager起動通知を行う.
     */
    private void sendLaunchedEvent() {
        sendManagerEvent(IntentDConnectMessage.ACTION_MANAGER_LAUNCHED);
    }

    /**
     * 全デバイスプラグインに対して、Device Connect Manager終了通知を行う.
     */
    private void sendTerminatedEvent() {
        sendManagerEvent(IntentDConnectMessage.ACTION_MANAGER_TERMINATED);
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
        builder.setContentText(DConnectUtil.getIPAddress() + ":" + mSettings.getPort());
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
        intent.putExtra(IntentDConnectMessage.EXTRA_PRODUCT, mProductName);
        intent.putExtra(IntentDConnectMessage.EXTRA_VERSION, mVersionName);

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
        Intent intent = createResponseIntent(request, response);
        if (intent.getComponent() == null) {
            return;
        }
        sendBroadcast(intent);
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
     * オリジン要求設定を取得する.
     * @return オリジンが必要な場合はtrue、それ以外はfalse
     */
    public boolean requiresOrigin() {
        return mSettings.requireOrigin();
    }

    /**
     * Local OAuth要求設定を取得する.
     * @return Local OAuthが必要な場合はtrue、それ以外はfalse
     */
    public boolean usesLocalOAuth() {
        return mSettings.isUseALocalOAuth();
    }

    /**
     * プラグインマネージャーを取得する.
     * @return プラグインマネージャー
     */
    public DevicePluginManager getPluginManager() {
        return mPluginManager;
    }

    /**
     * DeviceConnect設定を取得する.
     * @return DeviceConnect設定
     */
    public DConnectSettings getSettings() {
        return mSettings;
    }
}
