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
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        AuthorizationProfileConstants.PROFILE_NAME,
        SystemProfileConstants.PROFILE_NAME,
        ServiceDiscoveryProfileConstants.PROFILE_NAME
    };

    /** プラグイン側のService Discoveryのプロファイル名: {@value}. */
    private static final String PROFILE_NETWORK_SERVICE_DISCOVERY = "networkServiceDiscovery";

    /** プラグイン側のService Discoveryのアトリビュート名: {@value}. */
    private static final String ATTRIBUTE_GET_NETWORK_SERVICES = "getNetworkServices";

    /** プラグイン側のAuthorizationのアトリビュート名: {@value}. */
    private static final String ATTRIBUTE_CREATE_CLIENT = "createClient";

    /** プラグイン側のAuthorizationのアトリビュート名: {@value}. */
    private static final String ATTRIBUTE_REQUEST_ACCESS_TOKEN = "requestAccessToken";

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

    /**
     * SystemProfileを取得する.
     * SystemProfileは必須実装となるため、本メソッドでSystemProfileのインスタンスを渡すこと。
     * このメソッドで返却したSystemProfileは自動で登録される。
     * 
     * @return SystemProfileのインスタンス
     */
    protected abstract SystemProfile getSystemProfile();

    /**
     * ServiceInformationProfileを取得する.
     * ServiceInformationProfileは必須実装となるため、本メソッドでServiceInformationProfileのインスタンスを渡すこと。
     * このメソッドで返却したServiceInformationProfileは自動で登録される。
     * 
     * @return SystemProfileのインスタンス
     */
    protected abstract ServiceInformationProfile getServiceInformationProfile();

    /**
     * ServiceDiscoveryProfileを取得する.
     * ServiceDiscoveryProfileは必須実装となるため
     * 本メソッドでServiceDiscoveryProfileのインスタンスを渡すこと。
     * このメソッドで返却したServiceDiscoveryProfileは自動で登録される。
     * 
     * @return ServiceDiscoveryProfileのインスタンス
     */
    protected abstract ServiceDiscoveryProfile getServiceDiscoveryProfile();

    @Override
    public void onCreate() {
        super.onCreate();

        // LocalOAuthの初期化
        LocalOAuth2Main.initialize(this);

        // 認証プロファイルの追加
        addProfile(new AuthorizationProfile(this));
        // 必須プロファイルの追加
        addProfile(getSystemProfile());
        addProfile(getServiceInformationProfile());
        addProfile(getServiceDiscoveryProfile());
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
            onRequest(intent, MessageUtils.createResponseIntent(intent));
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

        // Service Discovery APIのパスを変換
        if (PROFILE_NETWORK_SERVICE_DISCOVERY.equals(profileName)) {
            profileName = ServiceDiscoveryProfileConstants.PROFILE_NAME;
            String attributeName = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
            if (ATTRIBUTE_GET_NETWORK_SERVICES.equals(attributeName)) {
                request.putExtra(DConnectMessage.EXTRA_PROFILE, profileName);
                request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, (String) null);
            }
        }
        // Authorization APIのパスを変換
        if (AuthorizationProfileConstants.PROFILE_NAME.equals(profileName)) {
            String attributeName = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
            if (ATTRIBUTE_CREATE_CLIENT.equals(attributeName)) {
                request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE,
                        AuthorizationProfileConstants.ATTRIBUTE_GRANT);
            } else if (ATTRIBUTE_REQUEST_ACCESS_TOKEN.equals(attributeName)) {
                request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE,
                        AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
            }
        }

        // プロファイルを取得する
        DConnectProfile profile = getProfile(profileName);
        if (profile == null) {
            MessageUtils.setNotSupportProfileError(response);
            sendResponse(response);
            return;
        }

        // 各プロファイルでリクエストを処理する
        boolean send = true;
        if (isUseLocalOAuth()) {
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
        return mProfileMap.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProfile(final DConnectProfile profile) {
        profile.setContext(this);
        mProfileMap.put(profile.getProfileName(), profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeProfile(final DConnectProfile profile) {
        mProfileMap.remove(profile.getProfileName());
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
}
