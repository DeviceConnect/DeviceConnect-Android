/*
 DConnectMessageService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.compat.AuthorizationRequestConverter;
import org.deviceconnect.android.compat.LowerCaseConverter;
import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.compat.ServiceDiscoveryRequestConverter;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectApiSpecFilter;
import org.deviceconnect.android.profile.spec.DConnectApiSpecList;
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
import java.util.Set;
import java.util.logging.Logger;

/**
 * Device Connectメッセージサービス.
 * 
 * <p>
 * Device Connectリクエストメッセージを受信し、Device Connectレスポンスメッセージを送信するサービスである。
 * {@link DConnectMessageServiceProvider}から呼び出されるサービスとし、UIレイヤーから明示的な呼び出しは行わない。
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
     * デフォルトではtrueにしておくこと。
     */
    private boolean mUseLocalOAuth = true;

    private DConnectServiceManager mServiceManager;

    private final MessageConverter[] mRequestConverters = {
        new ServiceDiscoveryRequestConverter(),
        new AuthorizationRequestConverter(),
        new LowerCaseConverter()
    };

    /**
     * SystemProfileを取得する.
     * SystemProfileは必須実装となるため、本メソッドでSystemProfileのインスタンスを渡すこと。
     * このメソッドで返却したSystemProfileは自動で登録される。
     * 
     * @return SystemProfileのインスタンス
     */
    protected abstract SystemProfile getSystemProfile();

    protected final DConnectServiceProvider getServiceProvider() {
        return mServiceManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mServiceManager = new DConnectServiceManager();
        mServiceManager.setApiSpecDictionary(loadApiSpecList());
        mServiceManager.setContext(getContext());

        // LocalOAuthの初期化
        LocalOAuth2Main.initialize(this);

        // 認証プロファイルの追加
        addProfile(new AuthorizationProfile(this));
        // 必須プロファイルの追加
        addProfile(new ServiceDiscoveryProfile(mServiceManager));
        addProfile(getSystemProfile());
    }

    private DConnectApiSpecList loadApiSpecList() {
        try {
            final Map<String, DevicePluginXmlProfile> supportedProfiles = DevicePluginXmlUtil.getSupportProfiles(this, getPackageName());
            final Set<String> profileNames = supportedProfiles.keySet();

            String[] fileNames = getAssets().list("api");
            final DConnectApiSpecList specList = new DConnectApiSpecList();
            final DConnectApiSpecFilter filter = new DConnectApiSpecFilter() {
                @Override
                public boolean filter(final DConnectApiSpec apiSpec) {
                    for (String profileName : profileNames) {
                        if (profileName.equalsIgnoreCase(apiSpec.getProfileName())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            for (String fileName : fileNames) {
                mLogger.info("Loading API Spec file...: " + fileName);
                specList.addApiSpecList(getAssets().open("api/" + fileName), filter);
                mLogger.info("Loaded API Spec file: " + fileName);
            }
            return specList;
        } catch (IOException e) {
            mLogger.warning("Failed to load Device Connect API Specs.");
            throw new RuntimeException(e);
        } catch (JSONException e) {
            mLogger.warning("Device Connect API Specs is invalid.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LocalOAuthの後始末
        LocalOAuth2Main.destroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
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

        if (checkRequestAction(action)) {
            convertRequest(intent);
            onRequest(intent, MessageUtils.createResponseIntent(intent));
        }

        if (checkManagerUninstall(intent)) {
            onManagerUninstalled();
        }

        if (checkManagerTerminate(action)) {
            onManagerTerminated();
        }

        if (checkManagerEventTransmitDisconnect(action)) {
            onManagerEventTransmitDisconnected(intent.getStringExtra(IntentDConnectMessage.EXTRA_SESSION_KEY));
        }

        if (checkDevicePluginReset(action)) {
            onDevicePluginReset();
        }
        return START_STICKY;
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
     * 受信したリクエストをプロファイルに振り分ける.
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

        // 指定されたサービスの各プロファイルでリクエストを処理する
        DConnectProfile profile = getProfile(profileName);
        boolean send = true;
        if (profile == null) {
            DConnectService service = mServiceManager.getService(DConnectProfile.getServiceID(request));
            if (service != null) {
                send = service.onRequest(request, response);
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
        }

        // プラグイン本体の各プロファイルでリクエストを処理する
        else if (isUseLocalOAuth()) {
            // アクセストークン
            String accessToken = request.getStringExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN);
            // LocalOAuth処理
            CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken, profileName,
                IGNORE_PROFILES);
            if (result.checkResult()) {
                send = profile.onRequest(request, response);
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
            send = profile.onRequest(request, response);
        }

        if (send) {
            sendResponse(response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DConnectProfile> getProfileList() {
        return new ArrayList<>(mProfileMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        //XXXX パスの大文字小文字の無視
        return mProfileMap.get(name.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        profile.setContext(this);
        //XXXX パスの大文字小文字の無視
        mProfileMap.put(profile.getProfileName().toLowerCase(), profile);
    }

    /**
     * {@inheritDoc}
     */
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

        getContext().sendBroadcast(response);
        return true;
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

        getContext().sendBroadcast(event);
        return true;
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
     * 
     * このフラグをfalseに設定することで、LocalOAuthの機能をOFFにすることができる。
     * デフォルトでは、trueになっているので、LocalOAuthが有効になっている。
     * 
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
     * Device Connect Managerがアンインストールされた時に呼ばれる処理部.
     */
    protected void onManagerUninstalled() {
        mLogger.info("SDK : onManagerUninstalled");
    }

    /**
     * Device Connect Managerの正常終了通知を受信した時に呼ばれる処理部.
     */
    protected void onManagerTerminated() {
        mLogger.info("SDK : on ManagerTerminated");
    }

    /**
     * Device Connect ManagerのEvent送信経路切断通知を受信した時に呼ばれる処理部.
     * @param sessionKey セッションキー
     */
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        mLogger.info("SDK : onManagerEventTransmitDisconnected");
    }

    /**
     * Device Plug-inへのReset要求を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginReset() {
        mLogger.info("SDK : onDevicePluginReset");
    }
}
