/*
 DConnectMessageService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.IDConnectPlugin;
import org.deviceconnect.android.compat.AuthorizationRequestConverter;
import org.deviceconnect.android.compat.LowerCaseConverter;
import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.compat.ServiceDiscoveryRequestConverter;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.EventCacheController;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceManager;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Device Connectメッセージサービス.
 * 
 * <p>
 * Device Connectリクエストメッセージを受信し、Device Connectレスポンスメッセージを送信するサービスである。<br>
 * {@link DConnectMessageServiceProvider}から呼び出されるサービスとし、UIレイヤーから明示的な呼び出しは行わない。
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectMessageService extends Service implements DConnectProfileProvider {

    /** 
     * LocalOAuthで無視するプロファイル群.
     */
    private static final String[] IGNORE_PROFILES = {
        AuthorizationProfileConstants.PROFILE_NAME.toLowerCase(),
        SystemProfileConstants.PROFILE_NAME.toLowerCase(),
        ServiceDiscoveryProfileConstants.PROFILE_NAME.toLowerCase()
    };

    /** プロファイル仕様定義ファイルの拡張子. */
    private static final String SPEC_FILE_EXTENSION = ".json";

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    /**
     * プロファイルインスタンスマップ.
     */
    private Map<String, DConnectProfile> mProfileMap = new HashMap<>();

    /**
     * Local OAuth使用フラグ.
     * <p>
     * デフォルトではtrueにしておくこと。
     * </p>
     */
    private boolean mUseLocalOAuth = true;

    /**
     * サービスを管理するクラス.
     */
    private DConnectServiceProvider mServiceProvider;

    /**
     * リクエストを変換するコンバータクラス.
     */
    private final MessageConverter[] mRequestConverters = {
        new ServiceDiscoveryRequestConverter(),
        new AuthorizationRequestConverter(),
        new LowerCaseConverter()
    };

    /**
     * プラグインのスペック.
     */
    private DConnectPluginSpec mPluginSpec;

    private final IBinder mLocalBinder = new LocalBinder();

    private final IBinder mRemoteBinder = new PluginBinder();

    private final Map<String, MessageSender> mBindingSenders = new ConcurrentHashMap<>();

    private final MessageSender mDefaultSender = new MessageSender() {
        @Override
        public void send(final Intent message) {
            sendBroadcast(message);
        }
    };

    private ScheduledExecutorService mExecutorService;

    private boolean mIsEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        setLogLevel();
        EventManager.INSTANCE.setController(getEventCacheController());

        mPluginSpec = loadPluginSpec();

        DConnectServiceManager serviceManager = new DConnectServiceManager();
        serviceManager.setPluginSpec(mPluginSpec);
        serviceManager.setContext(getContext());
        mServiceProvider = serviceManager;
        mExecutorService = Executors.newSingleThreadScheduledExecutor();

        // LocalOAuthの初期化
        LocalOAuth2Main.initialize(this);

        // 認証プロファイルの追加
        addProfile(new AuthorizationProfile(this));
        // 必須プロファイルの追加
        addProfile(new ServiceDiscoveryProfile(mServiceProvider));
        addProfile(getSystemProfile());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // スレッドの停止
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
        // LocalOAuthの後始末
        LocalOAuth2Main.destroy();
        // コールバック一覧を削除
        mBindingSenders.clear();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        mLogger.info("onBind: " + getClass().getName());
        if (isCalledFromLocal()) {
            mLogger.info("onBind: Local binder");
            return mLocalBinder;
        }
        mLogger.info("onBind: Remote binder");
        return mRemoteBinder;
    }

    private boolean isCalledFromLocal() {
        return getPackageName().equals(getCallingPackage());
    }

    private String getCallingPackage() {
        int uid = Binder.getCallingUid();
        return getPackageManager().getNameForUid(uid);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null) {
            mLogger.warning("request intent is null.");
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            mLogger.warning("request action is null. ");
            return START_STICKY;
        }

        handleMessage(intent);
        return START_STICKY;
    }

    private void handleMessage(final Intent intent) {
        String action = intent.getAction();
        if (checkRequestAction(action)) {
            onRequest(intent);
        }

        if (checkManagerUninstall(intent)) {
            onManagerUninstalled();
        }

        if (checkManagerTerminate(action)) {
            onManagerTerminated();
        }

        if (checkManagerEventTransmitDisconnect(action)) {
            onManagerEventTransmitDisconnected(intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN));
        }

        if (checkDevicePluginReset(action)) {
            onDevicePluginReset();
        }

        if (checkDevicePluginEnabled(action)) {
            mIsEnabled = true;
            onDevicePluginEnabled();
        }

        if (checkDevicePluginDisabled(action)) {
            mIsEnabled = false;
            onDevicePluginDisabled();
        }
    }

    /**
     * デバッグログの出力レベルを設定する.
     * <p>
     * デバッグフラグがfalseの場合には、ログを出力しないようにすること。
     * </p>
     */
    private void setLogLevel() {
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(mLogger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
            mLogger.setUseParentHandlers(false);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

    /**
     * サービスを管理するクラスを取得する.
     *
     * @return サービス管理クラス
     */
    public final DConnectServiceProvider getServiceProvider() {
        return mServiceProvider;
    }

    protected final void setServiceProvider(final DConnectServiceProvider provider) {
        mServiceProvider = provider;
    }

    protected final DConnectPluginSpec getPluginSpec() {
        return mPluginSpec;
    }

    private DConnectPluginSpec loadPluginSpec() {
        final Map<String, DevicePluginXmlProfile> supportedProfiles = DevicePluginXmlUtil.getSupportProfiles(this, getPackageName());

        final AssetManager assets = getAssets();
        final DConnectPluginSpec pluginSpec = new DConnectPluginSpec();
        for (Map.Entry<String, DevicePluginXmlProfile> entry : supportedProfiles.entrySet()) {
            String profileName = entry.getKey();
            DevicePluginXmlProfile profile = entry.getValue();
            try {
                List<String> dirList = new ArrayList<>();
                String assetsPath = profile.getSpecPath();
                if (assetsPath != null) {
                    dirList.add(assetsPath);
                }
                dirList.add("api");
                String filePath = null;
                for (String dir : dirList) {
                    String[] fileNames = assets.list(dir);
                    String fileName = findProfileSpecName(fileNames, profileName);
                    if (fileName != null) {
                        filePath = dir + "/" + fileName;
                        break;
                    }
                }
                if (filePath == null) {
                    throw new RuntimeException("Profile spec is not found: " + profileName);
                }
                pluginSpec.addProfileSpec(profileName.toLowerCase(), assets.open(filePath));
                mLogger.info("Loaded a profile spec: " + profileName);
            } catch (IOException | JSONException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            }
        }
        return pluginSpec;
    }

    private static String findProfileSpecName(final String[] fileNames, final String profileName) {
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
                return fileFullName;
            }
        }
        return null;
    }

    /**
     * 指定されたアクションがDevice Connectのアクションかチェックします.
     * @param action チェックするアクション
     * @return Device Connectのアクションの場合はtrue、それ以外はfalse
     */
    private boolean checkRequestAction(String action) {
        return IntentDConnectMessage.ACTION_GET.equals(action)
                || IntentDConnectMessage.ACTION_POST.equals(action)
                || IntentDConnectMessage.ACTION_PUT.equals(action)
                || IntentDConnectMessage.ACTION_DELETE.equals(action);
    }

    /**
     * リクエストのプロファイル名などを変換する.
     *
     * @param request 変換処理を行うリクエスト
     */
    private void convertRequest(final Intent request) {
        for (MessageConverter converter : mRequestConverters) {
            converter.convert(request);
        }
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
     * Device Connect Manager 正常終了通知を受信したかをチェックします.
     * @param action チェックするアクション
     * @return Manager 正常終了検知でtrue、それ以外はfalse
     */
    private boolean checkManagerTerminate(String action) {
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

    private void onRequest(final Intent request) {
        convertRequest(request);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                onRequest(request, MessageUtils.createResponseIntent(request));
            }
        });
    }

    private MessageSender getMessageSender(final Intent message) {
        ComponentName target = message.getComponent();
        String targetPackage = target.getPackageName();
        MessageSender sender = mBindingSenders.get(targetPackage);
        if (sender != null) {
            return sender;
        } else {
            return mDefaultSender;
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
            // アクセストークン
            String accessToken = request.getStringExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN);
            // LocalOAuth処理
            CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken, profileName,
                IGNORE_PROFILES);
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
     * リクエストを実行する.
     *
     * @param profileName プロファイル名
     * @param request リクエスト
     * @param response レスポンス
     * @return trueの場合には即座にレスポンスを返却する、それ以外の場合にはレスポンスを返却しない
     */
    protected boolean executeRequest(final String profileName, final Intent request,
                                   final Intent response) {
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

    @Override
    public List<DConnectProfile> getProfileList() {
        return new ArrayList<>(mProfileMap.values());
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        //XXXX パスの大文字小文字の無視
        return mProfileMap.get(name.toLowerCase());
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        String profileName = profile.getProfileName().toLowerCase();
        profile.setContext(this);
        DConnectProfileSpec profileSpec = mPluginSpec.findProfileSpec(profileName);
        if (profileSpec != null) {
            profile.setProfileSpec(profileSpec);
        }

        //XXXX パスの大文字小文字の無視
        mProfileMap.put(profileName, profile);
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        //XXXX パスの大文字小文字の無視
        mProfileMap.remove(profile.getProfileName().toLowerCase());
    }

    /**
     * コンテキストの取得する.
     * 
     * @return コンテキスト
     */
    public final Context getContext() {
        return this;
    }

    /**
     * Device Connect Managerにレスポンスを返却するためのメソッド.
     * @param response レスポンス
     * @return 送信成功の場合true、それ以外はfalse
     */
    public final boolean sendResponse(final Intent response) {
        // TODO チェックが必要な追加すること。
        if (response == null) {
            throw new IllegalArgumentException("response is null.");
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("sendResponse: " + response);
            mLogger.info("sendResponse Extra: " + response.getExtras());
        }

        MessageSender sender = getMessageSender(response);
        if (sender != null) {
            sender.send(response);
            return true;
        }
        return false;
    }

    /**
     * Device Connectにイベントを送信する.
     * 
     * @param event イベントパラメータ
     * @param accessToken 送り先のアクセストークン
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    public final boolean sendEvent(final Intent event, final String accessToken) {
        // TODO 返り値をもっと詳細なものにするか要検討
        if (event == null) {
            throw new IllegalArgumentException("Event is null.");
        }

        if (isUseLocalOAuth()) {
            CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken,
                    event.getStringExtra(DConnectMessage.EXTRA_PROFILE), IGNORE_PROFILES);
            if (!result.checkResult()) {
                return false;
            }
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("sendEvent: " + event);
            mLogger.info("sendEvent Extra: " + event.getExtras());
        }

        MessageSender sender = getMessageSender(event);
        if (sender != null) {
            sender.send(event);
            return true;
        }
        return false;
    }

    /**
     * Device Connectにイベントを送信する.
     *
     * @param event イベントパラメータ
     * @param bundle パラメータ
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    public final boolean sendEvent(final Event event, final Bundle bundle) {
        Intent intent = EventManager.createEventMessage(event);
        Bundle original = intent.getExtras();
        original.putAll(bundle);
        intent.putExtras(original);
        return sendEvent(intent, event.getAccessToken());
    }

    /**
     * Local OAuth使用フラグを設定する.
     * <p>
     * このフラグをfalseに設定することで、LocalOAuthの機能をOFFにすることができる。<br>
     * デフォルトでは、trueになっているので、LocalOAuthが有効になっている。
     * </p>
     * @param use フラグ
     */
    protected void setUseLocalOAuth(final boolean use) {
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
     * 指定されたプロファイルはLocal OAuth認証を無視して良いかを確認する.
     *
     * @param profileName プロファイル名
     * @return 無視して良い場合はtrue、それ以外はfalse
     */
    public boolean isIgnoredProfile(final String profileName) {
        for (String name : IGNORE_PROFILES) {
            if (name.equalsIgnoreCase(profileName)) { // MEMO パスの大文字小文字を無視
                return true;
            }
        }
        return false;
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
     * Device Connect Managerがアンインストールされた時に呼ばれる処理部.
     * <p>
     * Device Connect Managerがアンインストールされた場合に処理を行いたい場合には、
     * このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerUninstalled() {
        mLogger.info("SDK : onManagerUninstalled");
    }

    /**
     * Device Connect Managerの正常終了通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerが終了された場合に処理を行い場合には、このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerTerminated() {
        mLogger.info("SDK : on ManagerTerminated");
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
        mLogger.info("SDK : onManagerEventTransmitDisconnected: " + origin);
    }

    /**
     * Device Plug-inへのReset要求を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerからデバイスプラグインのリセット要求が送られてきた場合には、
     * このメソッドをオーバーライドして、再起動処理を行うこと。
     * </p>
     */
    protected void onDevicePluginReset() {
        mLogger.info("SDK : onDevicePluginReset");
    }

    /**
     * Device Connect Managerからプラグイン有効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginEnabled() {
        mLogger.info("SDK : onEnabled");
    }

    /**
     * Device Connect Managerからプラグイン無効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginDisabled() {
        mLogger.info("SDK : onDisabled");
    }

    /**
     * Device Connect Manager側で本プラグインが有効になっているかどうかを取得する.
     * @return 有効になっている場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    protected boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     * Serviceをバインドするためのクラス.
     * <p>
     * {@link org.deviceconnect.android.ui.activity.DConnectServiceListActivity}で、
     * サービス一覧をを取得するためにバインドされる。
     * </p>
     */
    public class LocalBinder extends IDConnectPlugin.Stub {

        private final IDConnectPlugin.Stub mDelegate = new PluginBinder();

        /**
         * DConnectMessageServiceのインスタンスを取得する.
         * <p>
         * 本メソッドはDevice Connect Manager内部のみで使用される.
         * </p>
         * @return DConnectMessageServiceのインスタンス
         */
        public DConnectMessageService getMessageService() {
            return DConnectMessageService.this;
        }

        @Override
        public void registerCallback(final IDConnectCallback callback) throws RemoteException {
            mDelegate.registerCallback(callback);
        }

        @Override
        public void sendMessage(final Intent message) throws RemoteException {
            mDelegate.sendMessage(message);
        }

        @Override
        public ParcelFileDescriptor readFileDescriptor(final String fileId) throws RemoteException {
            return mDelegate.readFileDescriptor(fileId);
        }
    }
    
    private class PluginBinder extends IDConnectPlugin.Stub {

        @Override
        public void registerCallback(final IDConnectCallback callback) throws RemoteException {
            mBindingSenders.put(getCallingPackage(), new MessageSender() {
                @Override
                public void send(final Intent message) {
                    try {
                        callback.sendMessage(message);
                    } catch (RemoteException e) {
                        // TODO マネージャへの応答に失敗した場合
                    }
                }
            });
        }

        @Override
        public void sendMessage(final Intent message) throws RemoteException {
            handleMessage(message);
        }

        @Override
        public ParcelFileDescriptor readFileDescriptor(final String fileId) throws RemoteException {
            return null; // 将来的に必要になった場合に実装.
        }
    }

    private interface MessageSender {
        void send(Intent response);
    }
}
