/*
 DevicePluginContext.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.EventCacheController;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceManager;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.android.ssl.EndPointKeyStoreManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.android.ssl.KeyStoreManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * プラグインのベースとなるクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DevicePluginContext implements DConnectProfileProvider, DConnectProfile.Responder {
    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    /**
     * LocalOAuthで無視するプロファイル群.
     */
    private static final String[] IGNORE_PROFILES = {
            AuthorizationProfileConstants.PROFILE_NAME.toLowerCase(),
            SystemProfileConstants.PROFILE_NAME.toLowerCase(),
            ServiceDiscoveryProfileConstants.PROFILE_NAME.toLowerCase()
    };

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * プロファイルインスタンスマップ.
     */
    private Map<String, DConnectProfile> mProfileMap = new HashMap<>();

    /**
     * Device Connect Manager へのメッセージを返すコールバック.
     */
    private IDConnectCallback mIDConnectCallback;

    /**
     * サービスを管理するクラス.
     */
    private DConnectServiceManager mServiceProvider;

    /**
     * プラグインが持つプロファイルスペック.
     */
    private DConnectPluginSpec mPluginSpec;

    /**
     * 認可クラス(Local OAuth).
     */
    private LocalOAuth2Main mLocalOAuth2Main;

    /**
     * Wi-Fiの接続が切り替わった通知を受け取るレシーバ.
     * <p>
     * IP アドレスが変わった場合に証明書を作り直す必要があるために使用します。
     * </p>
     */
    private BroadcastReceiver mWiFiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // 証明書を更新
            requestAndNotifyKeyStore();
        }
    };

    /**
     * キーストアを管理するクラス.
     */
    private KeyStoreManager mKeyStoreMgr;

    /**
     * Local OAuth使用フラグ.
     * <p>
     * デフォルトでは true にしておくこと。
     * </p>
     */
    private boolean mUseLocalOAuth = true;

    /**
     * プラグインの有効・無効設定.
     */
    private boolean mIsEnabled;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public DevicePluginContext(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }
        mContext = context;

        // イベント管理クラスの初期化
        EventManager.INSTANCE.setController(createEventCacheController());

        // LocalOAuthの初期化
        mLocalOAuth2Main = new LocalOAuth2Main(context);

        try {
            // プロファイルSPECを読み込み
            mPluginSpec = DConnectProfileHelper.loadPluginSpec(context, createSupportedProfiles());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                mLogger.warning("Failed to load a profile spec.");
            }
        }

        // キーストア管理クラスの初期化
        mKeyStoreMgr = new EndPointKeyStoreManager(context, getKeyStoreFileName(), getCertificateAlias());
        if (usesAutoCertificateRequest()) {
            requestAndNotifyKeyStore();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mWiFiBroadcastReceiver, filter);
        }

        // サービス管理クラス
        mServiceProvider = new DConnectServiceManager();
        mServiceProvider.setContext(context);
        mServiceProvider.setPluginContext(this);
        mServiceProvider.setPluginSpec(mPluginSpec);

        // プロファイルを追加
        addProfile(new AuthorizationProfile(this, mLocalOAuth2Main));
        addProfile(new ServiceDiscoveryProfile(mServiceProvider));
        addProfile(getSystemProfile());
    }

    /**
     * プラグインコンテキストの破棄を行います.
     * <p>
     * プラグインが破棄されるときに呼び出されます。<br>
     * 継承したクラスで必要に応じてオーバーライドすること。<br>
     * </p>
     * <p>
     * また、オーバーライドした場合には、super.release(); を呼び出すこと。
     * </p>
     */
    public void release() {
        if (mLocalOAuth2Main != null) {
            mLocalOAuth2Main.destroy();
            mLocalOAuth2Main = null;
        }
        if (usesAutoCertificateRequest()) {
            mContext.unregisterReceiver(mWiFiBroadcastReceiver);
        }

    }

    /**
     * コンテキストを取得します.
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * サービスを管理するクラスを取得する.
     *
     * @return サービス管理クラス
     */
    public DConnectServiceProvider getServiceProvider() {
        return mServiceProvider;
    }

    /**
     * プラグインが持っているプロファイルの仕様を取得します.
     *
     * @return プロファイルのサービス仕様
     */
    public DConnectPluginSpec getPluginSpec() {
        return mPluginSpec;
    }

    /**
     * Device Connect Manager に返答するためのコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setIDConnectCallback(final IDConnectCallback callback) {
        mIDConnectCallback = callback;
    }

    /**
     * LocalOAuthのインスタンスを取得します.
     * @return LocalOAuth
     */
    public LocalOAuth2Main getLocalOAuth2Main() {
        return mLocalOAuth2Main;
    }

    /**
     * サポートするプロファイルを取得します.
     *
     * @return サポートするプロファイル
     */
    private Map<String, DevicePluginXmlProfile> createSupportedProfiles() {
        return DevicePluginXmlUtil.getSupportProfiles(getContext(), getPluginXmlResId());
    }

    /**
     * サポートするプロファイルを定義しているxmlへのIDを取得します.
     *
     * @return サポートするプロファイル
     */
    protected int getPluginXmlResId() {
        return DevicePluginXmlUtil.getPluginXmlResourceId(getContext(), getContext().getPackageName());
    }

    /**
     * SystemProfileを取得する.
     * <p>
     * SystemProfileは必須実装となるため、本メソッドでSystemProfileのインスタンスを渡すこと。<br>
     * このメソッドで返却したSystemProfileは自動で登録される。
     * </p>
     * @return SystemProfileのインスタンス
     */
    protected abstract SystemProfile getSystemProfile();

    /**
     * リクエストなどのメッセージを受け取るメソッド.
     *
     * @param message リクエストなどの情報が格納されたIntent
     */
    public void handleMessage(final Intent message) {
        String action = message.getAction();
        try {
            if (checkRequestAction(action)) {
                // Device Connect リクエストの互換性を持たせるためにコンバートします
                MessageConverterHelper.convert(message);
                onRequest(message, MessageUtils.createResponseIntent(message));
            } else if (checkManagerUninstall(message)) {
                onManagerUninstalled();
            } else if (checkManagerLaunched(action)) {
                onManagerLaunched();
            } else if (checkManagerTerminated(action)) {
                onManagerTerminated();
            } else if (checkManagerEventTransmitDisconnect(action)) {
                onManagerEventTransmitDisconnected(message.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN));
            } else if (checkDevicePluginReset(action)) {
                onDevicePluginReset();
            } else if (checkDevicePluginEnabled(action)) {
                mIsEnabled = true;
                onDevicePluginEnabled();
            } else if (checkDevicePluginDisabled(action)) {
                mIsEnabled = false;
                onDevicePluginDisabled();
            } else {
                if (BuildConfig.DEBUG) {
                    mLogger.warning("action is unknown type. " + action);
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
                mLogger.severe("DevicePluginContext#handleMessage : error=" + e.getMessage());
            }
        }
    }

    /**
     * EventCacheControllerのインスタンスを返す.
     *
     * <p>
     * デフォルトではMemoryCacheControllerを使用する.<br>
     * 変更したい場合は本メソッドをオーバーライドすること.
     * </p>
     *
     * @return EventCacheControllerのインスタンス
     */
    protected EventCacheController getEventCacheController() {
        return new MemoryCacheController();
    }

    /**
     * EventCacheControllerのインスタンスを作成します.
     * <p>
     * デフォルトでは、MemoryCacheControllerを作成します。
     * </p>
     * @return EventCacheControllerのインスタンス
     */
    private EventCacheController createEventCacheController() {
        EventCacheController ctrl = getEventCacheController();
        if (ctrl == null) {
            ctrl = new MemoryCacheController();
        }
        return ctrl;
    }

    /**
     * Device Connect Manager側で本プラグインが有効になっているかどうかを取得する.
     * @return 有効になっている場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    protected boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     * Local OAuth使用フラグを設定する.
     * <p>
     * このフラグをfalseに設定することで、LocalOAuthの機能をOFFにすることができる。<br>
     * デフォルトでは、trueになっているので、LocalOAuthが有効になっている。
     * </p>
     * @param use フラグ
     */
    public void setUseLocalOAuth(final boolean use) {
        mUseLocalOAuth = use;
    }

    /**
     * Local OAuth使用フラグを取得する.
     *
     * @return 使用する場合にはtrue、それ以外はfalse
     */
    public boolean isUseLocalOAuth() {
        return mUseLocalOAuth;
    }

    /**
     * LocalOAuthで無視するプロファイル群.
     * プラグインごとに無視するプロファイルを選びたい場合は、このメソッドをオーバライドすること.
     *
     * @return LocalOAuthで無視するプロファイル群
     */
    public String[] getIgnoredProfiles() {
        return IGNORE_PROFILES;
    }
    /**
     * 指定されたプロファイルはLocal OAuth認証を無視して良いかを確認する.
     *
     * @param profileName プロファイル名
     * @return 無視して良い場合はtrue、それ以外はfalse
     */
    public boolean isIgnoredProfile(final String profileName) {
        for (String name : getIgnoredProfiles()) {
            if (name.equalsIgnoreCase(profileName)) { // MEMO パスの大文字小文字を無視
                return true;
            }
        }
        return false;
    }


    /// DConnectProfile#Responder Method


    /**
     * Device Connect Managerにレスポンスを返却するためのメソッド.
     *
     * @param response レスポンス
     * @return 送信成功の場合true、それ以外はfalse
     */
    @Override
    public boolean sendResponse(final Intent response) {
        if (response == null) {
            throw new IllegalArgumentException("response is null.");
        }

        // TODO チェックが必要な場合は追加すること。

        if (BuildConfig.DEBUG) {
            mLogger.info("sendResponse: " + response);
            mLogger.info("sendResponse Extra: " + response.getExtras());
        }

        return sendMessage(response);
    }

    /**
     * Device Connectにイベントを送信する.
     *
     * @param event イベントパラメータ
     * @param accessToken 送り先のアクセストークン
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    @Override
    public boolean sendEvent(final Intent event, final String accessToken) {
        // TODO 返り値をもっと詳細なものにするか要検討
        if (event == null) {
            throw new IllegalArgumentException("Event is null.");
        }

        if (isUseLocalOAuth()) {
            CheckAccessTokenResult result = mLocalOAuth2Main.checkAccessToken(accessToken,
                    event.getStringExtra(DConnectMessage.EXTRA_PROFILE), getIgnoredProfiles());
            if (!result.checkResult()) {
                return false;
            }
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("sendEvent: " + event);
            mLogger.info("sendEvent Extra: " + event.getExtras());
        }

        return sendMessage(event);
    }

    /**
     * Device Connectにイベントを送信する.
     *
     * @param event イベントパラメータ
     * @param bundle パラメータ
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    @Override
    public boolean sendEvent(final Event event, final Bundle bundle) {
        Intent intent = EventManager.createEventMessage(event);
        Bundle original = intent.getExtras();
        if (original != null) {
            original.putAll(bundle);
            intent.putExtras(original);
        }
        return sendEvent(intent, event.getAccessToken());
    }


    /// DConnectProfileProvider Method


    @Override
    public List<DConnectProfile> getProfileList() {
        return new ArrayList<>(mProfileMap.values());
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        // XXXX パスの大文字小文字を無視
        return name == null ? null : mProfileMap.get(name.toLowerCase());
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile != null) {
            profile.setContext(mContext);
            profile.setPluginContext(this);
            profile.setResponder(this);
            // XXXX パスの大文字小文字を無視
            mProfileMap.put(profile.getProfileName().toLowerCase(), profile);
        }
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile != null) {
            // XXXX パスの大文字小文字を無視
            mProfileMap.remove(profile.getProfileName().toLowerCase());
        }
    }

    /**
     * ブロードキャストで受信したリクエストをプロファイルに振り分ける.
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     */
    protected void onRequest(final Intent request, final Intent response) {
        if (BuildConfig.DEBUG) {
            mLogger.info("request: " + request);
            mLogger.info("request extras: " + request.getExtras());
        }

        // プロファイル名の取得
        String profileName = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        if (profileName == null) {
            MessageUtils.setNotSupportProfileError(response);
            sendResponse(response);
            return;
        }

        boolean send = true;
        if (isUseLocalOAuth()) {
            String accessToken = request.getStringExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN);
            CheckAccessTokenResult result = mLocalOAuth2Main.checkAccessToken(accessToken,
                    profileName.toLowerCase(), getIgnoredProfiles());
            if (result.checkResult()) {
                send = executeRequest(profileName, request, response);
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
            }
        } else {
            send = executeRequest(profileName, request, response);
        }

        if (send) {
            sendResponse(response);
        }
    }

    /**
     * リクエストを指定されたサービスに振り分けて実行する.
     * <p>
     * リクエストの処理を無理やりフックしたい場合には、このメソッドをオーバーライドしてください。
     * </p>
     * @param profileName プロファイル名
     * @param request リクエスト
     * @param response レスポンス
     * @return trueの場合には即座にレスポンスを返却する、それ以外の場合にはレスポンスを返却しない
     */
    protected boolean executeRequest(final String profileName, final Intent request, final Intent response) {
        DConnectProfile profile = getProfile(profileName);
        if (profile == null) {
            String serviceId = DConnectProfile.getServiceID(request);
            DConnectService service = getServiceProvider().getService(serviceId);
            if (service != null) {
                return service.onRequest(request, response);
            } else {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }
        } else {
            return profile.onRequest(request, response);
        }
    }

    /**
     * Intent を Device Connect Manager へ送信します.
     *
     * @param intent 送信するIntent
     * @return 送信成功した場合はtrue、それ以外はfalse
     */
    private boolean sendMessage(final Intent intent) {
        if (mIDConnectCallback == null) {
            if (BuildConfig.DEBUG) {
                mLogger.severe("sendMessage: IDConnectCallback is not set.");
            }
            return false;
        }

        try {
            mIDConnectCallback.sendMessage(intent);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                mLogger.severe("sendMessage: exception occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * 指定されたアクションがDevice Connectのアクションかチェックします.
     * @param action チェックするアクション
     * @return Device Connectのアクションの場合はtrue、それ以外はfalse
     */
    private boolean checkRequestAction(String action) {
        return IntentDConnectMessage.ACTION_GET.equals(action) ||
                IntentDConnectMessage.ACTION_POST.equals(action) ||
                IntentDConnectMessage.ACTION_PUT.equals(action) ||
                IntentDConnectMessage.ACTION_DELETE.equals(action);
    }

    /**
     * Device Connect Managerがアンインストールされたかをチェックします.
     * @param intent intentパラメータ
     * @return アンインストール時はtrue、それ以外はfalse
     */
    private boolean checkManagerUninstall(final Intent intent) {
        return Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction()) &&
                intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED) &&
                intent.getDataString().contains("package:org.deviceconnect.android.manager");
    }

    /**
     * Device Connect Manager 起動通知を受信したかをチェックします.
     * @param action チェックするアクション
     * @return Manager 起動検知でtrue、それ以外はfalse
     */
    private boolean checkManagerLaunched(String action) {
        return IntentDConnectMessage.ACTION_MANAGER_LAUNCHED.equals(action);
    }

    /**
     * Device Connect Manager 正常終了通知を受信したかをチェックします.
     * @param action チェックするアクション
     * @return Manager 正常終了検知でtrue、それ以外はfalse
     */
    private boolean checkManagerTerminated(String action) {
        return IntentDConnectMessage.ACTION_MANAGER_TERMINATED.equals(action);
    }

    /**
     * Device Connect Manager のEvent 送信経路切断通知を受信したかチェックします.
     * @param action チェックするアクション
     * @return 検知受信でtrue、それ以外はfalse
     */
    private boolean checkManagerEventTransmitDisconnect(String action) {
        return IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT.equals(action);
    }

    /**
     * Device Plug-inへのReset要求を受信したかチェックします.
     * @param action チェックするアクション
     * @return Reset要求受信でtrue、それ以外はfalse
     */
    private boolean checkDevicePluginReset(String action) {
        return IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET.equals(action);
    }

    /**
     * プラグイン有効通知を受信したかチェックします.
     * @param action チェックするアクション
     * @return プラグイン有効通知でtrue、それ以外はfalse
     */
    private boolean checkDevicePluginEnabled(String action) {
        return IntentDConnectMessage.ACTION_DEVICEPLUGIN_ENABLED.equals(action);
    }

    /**
     * プラグイン無効通知を受信したかチェックします.
     * @param action チェックするアクション
     * @return プラグイン無効通知でtrue、それ以外はfalse
     */
    private boolean checkDevicePluginDisabled(String action) {
        return IntentDConnectMessage.ACTION_DEVICEPLUGIN_DISABLED.equals(action);
    }

    /**
     * Device Connect Managerがアンインストールされた時に呼ばれる処理部.
     * <p>
     * Device Connect Managerがアンインストールされた場合に処理を行いたい場合には、
     * このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerUninstalled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerUninstalled");
        }
    }

    /**
     * Device Connect Managerの起動通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerが起動された場合に処理を行い場合には、このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerLaunched() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerLaunched");
        }
    }

    /**
     * Device Connect Managerの正常終了通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerが終了された場合に処理を行い場合には、このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerTerminated() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerTerminated");
        }
    }

    /**
     * Device Connect ManagerのEvent送信経路切断通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect ManagerでWebSocketなどが切断され、イベント停止要求が送られてきた場合には、
     * このメソッドをオーバーライドして、イベントの停止処理や後始末の処理を行うこと。
     * </p>
     * @param origin イベント停止が要求されたオリジン
     */
    protected void onManagerEventTransmitDisconnected(final String origin) {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerEventTransmitDisconnected: " + origin);
        }
    }

    /**
     * Device Plug-inへのReset要求を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerからデバイスプラグインのリセット要求が送られてきた場合には、
     * このメソッドをオーバーライドして、再起動処理を行うこと。
     * </p>
     */
    protected void onDevicePluginReset() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onDevicePluginReset");
        }
    }

    /**
     * Device Connect Managerからプラグイン有効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginEnabled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onEnabled");
        }
    }

    /**
     * Device Connect Managerからプラグイン無効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginDisabled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onDisabled");
        }
    }


    // ここからは証明書の処理


    /**
     * 証明書の自動要求を行うか確認します.
     * <p>
     * 使用する場合には、このメソッドをオーバーライドして、trueを返却します。
     * </p>
     * @return 自動要求を行う場合はtrue、それ以外はfalse
     */
    protected boolean usesAutoCertificateRequest() {
        return false;
    }

    /**
     * 証明書で使用するキーストアのファイル名を取得します.
     * <p>
     * デフォルトでは、keystore.p12 を使用します。
     * </p>
     * <p>
     * キーストアのファイル名を変更したい場合には、このメソッドをオーバーライドします。
     * </p>
     * @return キーストアのファイル名
     */
    protected String getKeyStoreFileName() {
        return "keystore.p12";
    }

    /**
     * 証明書で使用するエイリアス名を取得します.
     * <p>
     * デフォルトでは、パッケージ名を返却します。
     * </p>
     * <p>
     * エイリアス名を変更したい場合には、このメソッドをオーバーライドします。
     * </p>
     * @return エイリアス名
     */
    protected String getCertificateAlias() {
        return getContext().getPackageName();
    }

    /**
     * キーストアの作成要求を行います.
     *
     * @param ipAddress IPアドレス
     * @param callback 作成結果コールバック
     */
    public void requestKeyStore(final String ipAddress, final KeyStoreCallback callback) {
        mKeyStoreMgr.requestKeyStore(ipAddress, callback);
    }

    /**
     * キーストアの要求と通知を行います.
     */
    private void requestAndNotifyKeyStore() {
        requestAndNotifyKeyStore(getCurrentIPAddress());
    }

    /**
     * キーストアの要求と通知を行います.
     *
     * @param ipAddress IPアドレス
     */
    private void requestAndNotifyKeyStore(final String ipAddress) {
        requestKeyStore(ipAddress, new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
                onKeyStoreUpdated(keyStore, cert, rootCert);
            }

            @Override
            public void onError(final KeyStoreError error) {
                onKeyStoreUpdateError(error);
            }
        });
    }

    /**
     * 現在のIPアドレスを取得します.
     *
     * @return IPアドレス
     */
    private String getCurrentIPAddress() {
        Context appContext = getContext().getApplicationContext();
        int state = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_WIFI_STATE);
        if (state != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        // TODO IPv6 の場合の処理が未実装
        // TODO Wi-Fi 以外で接続されていた場合が見実装

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
        return null;
    }

    /**
     * キーストアが更新された場合に呼び出されます.
     * <p>
     * キーストアが更新された場合に処理を行いたい場合には、このメソッドをオーバーライドします。
     * </p>
     * @param keyStore キーストア
     * @param cert 証明書
     * @param rootCert ルート証明書
     */
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
    }

    /**
     * キーストアの更新に失敗した場合に呼び出されます.
     * <p>
     * キーストアの更新に失敗した場合に処理を行いたい場合には、このメソッドをオーバーライドします。
     * </p>
     * @param error エラー
     */
    protected void onKeyStoreUpdateError(final KeyStoreError error) {
    }

    /**
     * SSLContext のインスタンスを作成します.
     * <p>
     * プラグイン内で Web サーバを立ち上げて、Managerと同じ証明書を使いたい場合にはこのSSLContext を使用します。
     * </p>
     * @param keyStore キーストア
     * @return SSLContextのインスタンス
     * @throws GeneralSecurityException SSLContextの作成に失敗した場合に発生
     */
    protected SSLContext createSSLContext(final KeyStore keyStore) throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "0000".toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom()
        );
        return sslContext;
    }

    public void regsiterChangeIpAddress() {
        if (usesAutoCertificateRequest()) {
            IntentFilter filter = new IntentFilter();
            mContext.registerReceiver(mWiFiBroadcastReceiver, filter);
        }
    }

    public void unregsiterChangeIpAddress() {
        if (usesAutoCertificateRequest()) {
            try {
                mContext.unregisterReceiver(mWiFiBroadcastReceiver);
            } catch (Exception e) {
                // ignore.
            }
        }
    }
}
