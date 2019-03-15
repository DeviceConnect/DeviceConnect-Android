/*
 DConnectDeliveryProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.manager.core.compat.CompatibleRequestConverter;
import org.deviceconnect.android.manager.core.compat.ServiceDiscoveryConverter;
import org.deviceconnect.android.manager.core.compat.ServiceInformationConverter;
import org.deviceconnect.android.manager.core.event.AbstractEventSessionFactory;
import org.deviceconnect.android.manager.core.event.EventBroker;
import org.deviceconnect.android.manager.core.event.EventSessionTable;
import org.deviceconnect.android.manager.core.event.KeepAliveManager;
import org.deviceconnect.android.manager.core.hmac.HmacManager;
import org.deviceconnect.android.manager.core.plugin.ConnectionState;
import org.deviceconnect.android.manager.core.plugin.DefaultConnectionFactory;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.plugin.MessagingException;
import org.deviceconnect.android.manager.core.plugin.PluginDetectionException;
import org.deviceconnect.android.manager.core.policy.OriginValidator;
import org.deviceconnect.android.manager.core.profile.AuthorizationProfile;
import org.deviceconnect.android.manager.core.profile.DConnectAvailabilityProfile;
import org.deviceconnect.android.manager.core.profile.DConnectDeliveryProfile;
import org.deviceconnect.android.manager.core.profile.DConnectServiceDiscoveryProfile;
import org.deviceconnect.android.manager.core.profile.DConnectSystemProfile;
import org.deviceconnect.android.manager.core.request.DConnectRequestManager;
import org.deviceconnect.android.manager.core.request.RegisterNetworkServiceDiscovery;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.deviceconnect.android.manager.core.plugin.ConnectionType.BROADCAST;

/**
 * Device Connect のメイン処理を行うクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectCore extends DevicePluginContext {
    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * 起動用URIスキーム名.
     */
    private static final String SCHEME_LAUNCH = "dconnect";

    /**
     * Device Connect サーバの設定.
     */
    private DConnectSettings mSettings;

    /**
     * デバイスプラグイン管理クラス.
     */
    private DevicePluginManager mPluginManager;

    /**
     * リクエスト管理クラス.
     */
    private DConnectRequestManager mRequestManager;

    /**
     * Origin妥当性確認クラス.
     */
    private OriginValidator mOriginValidator;

    /**
     * Local OAuthのデータを管理するクラス.
     * <p>
     * Manager ---> Plugin の LocalOAuthデータを格納します。
     * </p>
     */
    private DConnectLocalOAuth mLocalOAuth;

    /**
     * イベントセッション管理テーブル.
     */
    private EventSessionTable mEventSessionTable = new EventSessionTable();

    /**
     * イベントブローカー.
     */
    private EventBroker mEventBroker;

    /**
     * イベントKeep Alive管理クラス.
     */
    private KeepAliveManager mKeepAliveManager;

    /**
     * ファイル管理用プロバイダ.
     */
    private FileManager mFileMgr;

    /**
     * HMAC管理クラス.
     */
    private HmacManager mHmacManager;

    /**
     * 最後に処理されるプロファイル.
     */
    private DConnectProfile mDeliveryProfile;

    /**
     * リクエストのパスを変換するクラス群.
     */
    private MessageConverter[] mRequestConverters;

    /**
     * レスポンスのパスを変換するクラス群.
     */
    private MessageConverter[] mResponseConverters;

    /**
     * プラグインからの返答を受け取るコールバック.
     * <p>
     * Serviceにバインドしている場合に使用します。
     * </p>
     */
    private IDConnectCallback mCallback = new IDConnectCallback.Stub() {
        @Override
        public void sendMessage(final Intent message) {
            onReceivedMessage(message);
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param settings Device Connect Manager の設定
     * @throws IllegalArgumentException コンテキストがnullの場合に発生
     */
    DConnectCore(final Context context, final DConnectSettings settings, final AbstractEventSessionFactory factory) {
        super(context);

        if (settings == null) {
            throw new IllegalArgumentException("settings is null.");
        }
        mSettings = settings;

        // デバイスプラグインとのLocal OAuth情報
        mLocalOAuth = new DConnectLocalOAuth(context);

        // リクエスト管理クラス
        mRequestManager = new DConnectRequestManager(context);

        // ファイル管理クラス
        mFileMgr = new FileManager(context);

        // プラグイン管理クラスの初期化
        mPluginManager = new DevicePluginManager(context, DConnectConst.LOCALHOST_DCONNECT);
        mPluginManager.addEventListener(new DevicePluginManager.DevicePluginEventListener() {
            @Override
            public void onDeviceFound(final DevicePlugin plugin) {
                if (mSettings.isRegisterNetworkServiceDiscovery()) {
                    // 見つけたプラグインを有効にする
                    plugin.apply();
//                    if (plugin.isEnabled()) {
//                        RegisterNetworkServiceDiscovery req = new RegisterNetworkServiceDiscovery();
//                        req.setContext(context);
//                        req.setDestination(plugin);
//                        req.setDevicePluginManager(mPluginManager);
//                        if (mRequestManager != null) {
//                            mRequestManager.addRequest(req);
//                        }
//                    }
                }
                getServiceProvider().addService(plugin);
            }

            @Override
            public void onDeviceLost(final DevicePlugin plugin) {
                mEventBroker.removeSessionForPlugin(plugin.getPluginId());
                mLocalOAuth.deleteOAuthDatas(plugin.getPluginId());
                getServiceProvider().removeService(plugin);
            }

            @Override
            public void onConnectionStateChanged(final DevicePlugin plugin, final ConnectionState state) {
            }
        });
        mPluginManager.setConnectionFactory(new DefaultConnectionFactory(context, mCallback));

        mKeepAliveManager = new KeepAliveManager(getContext(), mEventSessionTable);
        mKeepAliveManager.setKeepAliveFunction(mSettings.isEnableKeepAlive());

        // イベントブローカの初期化
        mEventBroker = new EventBroker(mSettings, mEventSessionTable, mLocalOAuth, mPluginManager, factory);
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

        // プロファイルの追加
        addProfile(new AuthorizationProfile(mSettings, mRequestManager, getLocalOAuth2Main()));
        addProfile(new DConnectAvailabilityProfile(mSettings));
        addProfile(new DConnectServiceDiscoveryProfile(null, mPluginManager, mRequestManager));

        // 各プラグインに配送するプロファイル
        mDeliveryProfile = new DConnectDeliveryProfile(mPluginManager, mRequestManager,
                mLocalOAuth, mEventBroker, mSettings.requireOrigin());
        mDeliveryProfile.setContext(context);
        mDeliveryProfile.setPluginContext(this);
        mDeliveryProfile.setResponder(this);

        mRequestConverters = new MessageConverter[]{
                new CompatibleRequestConverter(getPluginManager())
        };
        mResponseConverters = new MessageConverter[]{
                new ServiceDiscoveryConverter(),
                new ServiceInformationConverter()
        };
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new DConnectSystemProfile(this);
    }

    @Override
    protected int getPluginXmlResId() {
        return R.xml.org_deviceconnect_android_manager;
    }

    @Override
    public String[] getIgnoredProfiles() {
        return DConnectLocalOAuth.IGNORE_PROFILES;
    }
    /**
     * DConnectSettings のインスタンスを取得します.
     *
     * @return DConnectSettings のインスタンス
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
        return mKeepAliveManager;
    }

    /**
     * プラグイン管理クラスを取得します.
     *
     * @return プラグイン管理クラス
     */
    public DevicePluginManager getPluginManager() {
        return mPluginManager;
    }
    /**
     * リクエスト管理クラスを取得します.
     *
     * @return リクエスト管理クラス
     */
    public DConnectRequestManager getRequestManager() {
        return mRequestManager;
    }
    /**
     * イベント配送クラスを取得します.
     *
     * @return イベント配送クラス
     */
    public EventBroker getEventBroker() {
        return mEventBroker;
    }

    /**
     * ファイル管理クラスを取得します.
     *
     * @return ファイル管理クラス
     */
    public FileManager getFileMgr() {
        return mFileMgr;
    }

    /**
     * プラグインを検索して、新規に見つけたプラグインをリストに追加します.
     * <p>
     * この処理は、時間がかかるので、別スレッドで処理することを推奨。
     * </p>
     */
    public void searchPlugin() {
        try {
            mPluginManager.createDevicePluginList();
        } catch (PluginDetectionException e) {
            // ignore.
        }
    }

    public void setDConnectInterface(final DConnectInterface i) {
        DConnectSystemProfile systemProfile = (DConnectSystemProfile) getProfile(SystemProfileConstants.PROFILE_NAME);
        if (systemProfile != null) {
            systemProfile.setDConnectInterface(i);
        }
        mKeepAliveManager.setDConnectInterface(i);
        mRequestManager.setDConnectInterface(i);

    }

    /**
     * プラグインからメッセージを受け取った時の処理を行います.
     *
     * @param message メッセージ(レスポンス・イベントなど)
     */
    void onReceivedMessage(final Intent message) {
        if (DConnectUtil.checkActionResponse(message)) {
            onReceivedResponse(message);
        } else if (DConnectUtil.checkActionEvent(message)) {
            onReceivedEvent(message);
        } else {
            if (BuildConfig.DEBUG) {
                mLogger.warning("Unknown message type.");
            }
        }
    }

    /**
     * プラグインの有効・無効を設定します.
     *
     * @param pluginId プラグインID
     * @param enable   trueの場合は有効、falseの場合は無効
     */
    public void setEnablePlugin(final String pluginId, final boolean enable) {
        final DevicePlugin plugin = mPluginManager.getDevicePlugin(pluginId);
        if (plugin != null) {
            if (enable) {
                plugin.enable();
            } else {
                plugin.disable();
            }
        }
    }

    /**
     * HMAC キーをアップデートします.
     *
     * @param origin オリジン
     * @param key    キー
     */
    public void updateHmacKey(final String origin, final String key) {
        if (mHmacManager != null && key != null && !TextUtils.isEmpty(origin)) {
            mHmacManager.updateKey(origin, key);
        }
    }

    /**
     * DConnectCore を開始します.
     */
    public void start() {
        // Local OAuth の設定を行う
        setUseLocalOAuth(mSettings.isUseALocalOAuth());

        mOriginValidator = new OriginValidator(getContext(),
                mSettings.requireOrigin(), mSettings.isBlockingOrigin());

        mHmacManager = new HmacManager(getContext());
        mPluginManager.startMonitoring();

        mRequestManager.start();

        sendLaunchedEvent();
    }

    /**
     * DConnectCore を停止します.
     */
    public void stop() {
        sendTerminatedEvent();

        mPluginManager.stopMonitoring();
        mPluginManager.disconnectAllPlugins();
        mRequestManager.stop();

        mHmacManager = null;
        mOriginValidator = null;
    }

    /**
     * DConnectCore の動作状況を確認します.
     *
     * @return 動作中はtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return mHmacManager != null && mOriginValidator != null;
    }

    /**
     * 全デバイスプラグインに対して、Device Connect Managerのライフサイクルについての通知を行う.
     */
    private void sendManagerEvent(final String action, final Bundle extras) {
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
            String message = "Skipped sending " + action + ": " + skipped.size() + " plugin(s)";
            if (skipped.size() > 0) {
                message += " below\n" + skipped;
            }
            mLogger.info(message);
        }
    }

    /**
     * Device Connect Manager のライフサイクルイベントを全てのプラグインに送信します.
     *
     * @param action アクション
     */
    private void sendManagerEvent(final String action) {
        sendManagerEvent(action, null);
    }

    /**
     * プラグインに対してメッセージを送信します.
     *
     * @param plugin  プラグイン
     * @param message メッセージ
     */
    private void sendMessage(final DevicePlugin plugin, final Intent message) {
        try {
            plugin.send(message);
        } catch (MessagingException e) {
            mLogger.warning("Failed to send event: action = " + message.getAction() + ", destination = " + plugin.getComponentName());
        }
    }

    /**
     * WebSocket の切断イベントを送信します.
     *
     * @param origin 切断されたWebSocketのオリジン
     */
    public void sendTransmitDisconnectEvent(final String origin) {
        Bundle extras = new Bundle();
        extras.putString(IntentDConnectMessage.EXTRA_ORIGIN, origin);
        sendManagerEvent(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT, extras);
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

    @Override
    public void handleMessage(final Intent message) {
        String action = message.getAction();
        String scheme = message.getScheme();
        if (SCHEME_LAUNCH.equals(scheme)) {
            String origin = message.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            String key = message.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
            updateHmacKey(origin, key);
        } else if (IntentDConnectMessage.ACTION_RESPONSE.equals(action)) {
            onReceivedResponse(message);
        } else if (IntentDConnectMessage.ACTION_EVENT.equals(action)) {
            onReceivedEvent(message);
        } else {
            super.handleMessage(message);
        }
    }

    @Override
    protected void onRequest(final Intent request, final Intent response) {
        // 不要になったキャッシュファイルの削除を行う
        if (mFileMgr != null) {
            mFileMgr.checkAndRemove();
        }

        // オリジンの正当性チェック
        String profileName = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        OriginValidator.OriginError error = mOriginValidator.checkOrigin(origin);
        switch (error) {
            default:
                MessageUtils.setUnknownError(response, "An unknown error occurred in checked the origin.");
                sendResponse(response);
                return;
            case NOT_SPECIFIED:
                MessageUtils.setInvalidOriginError(response, "Origin is not specified.");
                sendResponse(response);
                return;
            case NOT_UNIQUE:
                MessageUtils.setInvalidOriginError(response, "The specified origin is not unique.");
                sendResponse(response);
                return;
            case NOT_ALLOWED:
                // NOTE: Local OAuth関連のAPIに対する特別措置
                DConnectProfile profile = getProfile(profileName);
                if (profile != null && profile instanceof AuthorizationProfile) {
                    ((AuthorizationProfile) profile).onInvalidOrigin(request, response);
                }
                MessageUtils.setInvalidOriginError(response, "The specified origin is not allowed.");
                sendResponse(response);
                return;
            case NONE:
                if (origin == null && !mSettings.requireOrigin()) {
                    // オリジンがなく、オリジン設定が無効の場合には、<anonymous> を追加
                    request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, DConnectConst.ANONYMOUS_ORIGIN);
                }
                break;
        }

        super.onRequest(request, response);
    }

    @Override
    protected boolean executeRequest(final String profileName, final Intent request, final Intent response) {
        // リクエストにDeviceConnectManagerの情報を付加する
        request.putExtra(DConnectMessage.EXTRA_PRODUCT, mSettings.getProductName());
        request.putExtra(DConnectMessage.EXTRA_VERSION, mSettings.getVersionName());

        DConnectProfile profile = getProfile(request);
        if (profile != null && !isDeliveryRequest(request)) {
            return profile.onRequest(request, response);
        } else {
            //XXXX パスの互換性を担保
            for (MessageConverter converter : mRequestConverters) {
                converter.convert(request);
            }
            return mDeliveryProfile.onRequest(request, response);
        }
    }

    /**
     * レスポンス受信ハンドラー.
     *
     * @param response レスポンス用Intent
     */
    private void onReceivedResponse(final Intent response) {
        // レスポンスをリクエスト管理クラスに渡す
        if (mRequestManager != null) {
            mRequestManager.setResponse(response);
        }
    }

    /**
     * イベントメッセージ受信ハンドラー.
     *
     * @param event イベント用Intent
     */
    private void onReceivedEvent(final Intent event) {
        if (mEventBroker != null) {
            mEventBroker.onEvent(event);
        }
    }

    /**
     * レスポンス用のIntentを作成する.
     *
     * @param request  リクエスト
     * @param response リクエストに対応するレスポンス
     * @return 送信するレスポンス用Intent
     */
    public Intent createResponseIntent(final Intent request, final Intent response) {
        int requestCode = request.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, DConnectConst.ERROR_CODE);
        ComponentName cn = request
                .getParcelableExtra(IntentDConnectMessage.EXTRA_RECEIVER);

        Intent intent = new Intent(response);
        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(IntentDConnectMessage.EXTRA_PRODUCT, mSettings.getProductName());
        intent.putExtra(IntentDConnectMessage.EXTRA_VERSION, mSettings.getVersionName());

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

        //XXXX パスの互換性の担保
        for (MessageConverter converter : mResponseConverters) {
            converter.convert(intent);
        }
        return intent;
    }


    /**
     * 指定されたアクセストークンのOriginを取得する.
     *
     * @param accessToken アクセストークン
     * @return Origin
     */
    private String findOrigin(final String accessToken) {
        ClientPackageInfo packageInfo = getLocalOAuth2Main().findClientPackageInfoByAccessToken(accessToken);
        if (packageInfo == null) {
            return null;
        }
        // Origin is a package name of LocalOAuth client.
        return packageInfo.getPackageInfo().getPackageName();
    }

    /**
     * 指定されたリクエストがデバイスプラグインに配送するリクエストか確認する.
     * <p>
     * /system/deviceで、デバイス側に配信する必要がある。
     * </p>
     *
     * @param request リクエスト
     * @return プラグインに配送する場合にはtrue、それ以外はfalse
     */
    private boolean isDeliveryRequest(final Intent request) {
        return DConnectSystemProfile.isWakeUpRequest(request);
    }

    /**
     * リクエスト用Intentからプロファイルを取得する.
     *
     * @param request リクエスト用Intent
     * @return プロファイル
     */
    private DConnectProfile getProfile(final Intent request) {
        return getProfile(DConnectProfile.getProfile(request));
    }

    /**
     * Keep Alive のサポート確認を行います.
     *
     * @param plugin プラグイン
     * @return Keep Aliveが有効な場合はtrue、それ以外はfalse
     */
    private boolean isSupportedKeepAlive(final DevicePlugin plugin) {
        if (plugin.getConnectionType() != BROADCAST) {
            return false;
        }
        VersionName version = plugin.getPluginSdkVersionName();
        VersionName match = VersionName.parse("1.1.0");
        return !(version.compareTo(match) == -1);
    }
}
