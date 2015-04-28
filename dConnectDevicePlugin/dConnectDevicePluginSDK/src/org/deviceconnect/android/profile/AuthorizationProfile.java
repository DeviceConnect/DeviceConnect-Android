/*
 AuthorizationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.localoauth.AccessTokenData;
import org.deviceconnect.android.localoauth.AccessTokenScope;
import org.deviceconnect.android.localoauth.ClientData;
import org.deviceconnect.android.localoauth.ConfirmAuthParams;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.PublishAccessTokenListener;
import org.deviceconnect.android.localoauth.exception.AuthorizatonException;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.restlet.ext.oauth.PackageInfoOAuth;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

/**
 * Authorization プロファイル.
 * 
 * <p>
 * Local OAuthの認可機能を提供するAPI.<br/>
 * Local OAuthの認可機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class AuthorizationProfile extends DConnectProfile implements AuthorizationProfileConstants {

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    /**
     * プロファイルプロバイダー.
     */
    private final DConnectProfileProvider mProvider;

    /**
     * 指定されたプロファイルプロバイダーをもつAuthorizationプロファイルを生成する.
     * 
     * @param provider プロファイルプロバイダー
     */
    public AuthorizationProfile(final DConnectProfileProvider provider) {
        this.mProvider = provider;
    }

    /**
     * デバイスプラグインのサポートするすべてのプロファイル名の配列を取得する.
     * @return プロファイル名の配列
     */
    private String[] getAllProfileNames() {
        List<DConnectProfile> profiles = mProvider.getProfileList();
        String[] names = new String[profiles.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = profiles.get(i).getProfileName();
        }
        return names;
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected final boolean onGetRequest(final Intent request, final Intent response) {
        // Local OAuthを使用しない場合にはNot Supportを返却する
        DConnectMessageService service = (DConnectMessageService) getContext();
        if (!service.isUseLocalOAuth()) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        boolean send = true;
        String attribute = getAttribute(request);
        if (ATTRIBUTE_GRANT.equals(attribute)) {
            send = onGetCreateClient(request, response);
        } else if (ATTRIBUTE_ACCESS_TOKEN.equals(attribute)) {
            send = onGetRequestAccessToken(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
            send = true;
        }
        return send;
    }

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * 
     * @return レスポンスパラメータを送信するか否か
     */
    private boolean onGetCreateClient(final Intent request, final Intent response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createClient(request, response);
                } catch (Exception e) {
                    MessageUtils.setAuthorizationError(response, e.getMessage());
                }
                getContext().sendBroadcast(response);
            }
        }).start();
        return false;
    }

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * 
     * @return レスポンスパラメータを送信するか否か
     */
    private boolean onGetRequestAccessToken(final Intent request, final Intent response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getAccessToken(request, response);
                } catch (AuthorizatonException e) {
                    MessageUtils.setAuthorizationError(response, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (IllegalArgumentException e) {
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (IllegalStateException e) {
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (Exception e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getContext().sendBroadcast(response);
            }
        }).start();
        return false;
    }

    /**
     * Clientデータを作成する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    private void createClient(final Intent request, final Intent response) {
        String packageName = request.getStringExtra(AuthorizationProfile.PARAM_PACKAGE);
        String serviceId = request.getStringExtra(DConnectProfile.PARAM_SERVICE_ID);
        if (packageName == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            // Local OAuthでクライアント作成
            PackageInfoOAuth packageInfo = new PackageInfoOAuth(packageName, serviceId);
            try {
                ClientData client = LocalOAuth2Main.createClient(packageInfo);
                if (client != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    response.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, client.getClientId());
                } else {
                    MessageUtils.setAuthorizationError(response, "Cannot create a client.");
                }
            } catch (AuthorizatonException e) {
                MessageUtils.setAuthorizationError(response, e.getMessage());
            } catch (IllegalArgumentException e) {
                MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
            }
        }
    }

    /**
     * アクセストークンの取得の処理を行う.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * 
     * @throws AuthorizatonException 認証に失敗した場合に発生
     * @throws UnsupportedEncodingException 文字のエンコードに失敗した場合に発生
     */
    private void getAccessToken(final Intent request, final Intent response) 
            throws AuthorizatonException, UnsupportedEncodingException {
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String clientId = request.getStringExtra(AuthorizationProfile.PARAM_CLIENT_ID);
        String[] scopes = parseScopes(request.getStringExtra(AuthorizationProfile.PARAM_SCOPE));
        if (scopes == null) {
            scopes = getAllProfileNames();
        }

        String applicationName = null;
        PackageManager pm = getContext().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(getContext().getPackageName(), 0);
            ApplicationInfo ai = packageInfo.applicationInfo;
            applicationName = (String) pm.getApplicationLabel(ai);
        } catch (NameNotFoundException e) {
            applicationName = request.getStringExtra(AuthorizationProfile.PARAM_APPLICATION_NAME);
        }

        // TODO _typeからアプリorデバイスプラグインかを判別できる？
        ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(getContext()).serviceId(serviceId)
                .clientId(clientId).scopes(scopes).applicationName(applicationName)
                .isForDevicePlugin(true) 
                .build();

        // Local OAuthでAccessTokenを作成する。
        final AccessTokenData[] token = new AccessTokenData[1];
        LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
            @Override
            public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                token[0] = accessTokenData;
                synchronized (mLockObj) {
                    mLockObj.notifyAll();
                }
            }
            @Override
            public void onReceiveException(final Exception exception) {
                token[0] = null;
                synchronized (mLockObj) {
                    mLockObj.notifyAll();
                }
            }
        });

        // ユーザからのレスポンスを待つ
        if (token[0] == null) {
            waitForResponse();
        }

        // アクセストークンの確認
        if (token[0] != null && token[0].getAccessToken() != null) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            response.putExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN, token[0].getAccessToken());
            AccessTokenScope[] atScopes = token[0].getScopes();
            if (atScopes != null) {
                List<Bundle> s = new ArrayList<Bundle>();
                AccessTokenScope minScope = null;
                for (int i = 0; i < atScopes.length; i++) {
                    Bundle b = new Bundle();
                    b.putString(PARAM_SCOPE, atScopes[i].getScope());
                    b.putLong(PARAM_EXPIRE_PERIOD, atScopes[i].getExpirePeriod());
                    s.add(b);
                    
                    if (minScope == null || (minScope.getExpirePeriod() > atScopes[i].getExpirePeriod())) {
                        minScope = atScopes[i];
                    }
                }
                response.putExtra(PARAM_SCOPES, s.toArray(new Bundle[s.size()]));
                
                // NOTE: GotAPI 1.0対応
                if (minScope != null) {
                    response.putExtra(PARAM_EXPIRE, token[0].getTimestamp() + minScope.getExpirePeriod());
                }
            }
        } else {
            MessageUtils.setAuthorizationError(response, "Cannot create a access token.");
        }
    }

    /**
     * レスポンスが返ってくるまでの間スレッドを停止する.
     * タイムアウトは設定していない。
     */
    private void waitForResponse() {
        synchronized (mLockObj) {
            try {
                mLockObj.wait();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * スコープを分割して、配列に変換します.
     * @param scope スコープ
     * @return 分割されたスコープ. scopeが<code>null</code>または空文字の場合は<code>null</code>
     */
    private String[] parseScopes(final String scope) {
        if (scope == null) {
            return null;
        }
        String[] scopes = scope.split(",");
        for (int i = 0; i < scopes.length; i++) {
            String s = scopes[i].trim();
            if (s.equals("")) {
                return null;
            }
            scopes[i] = s;
        }
        return scopes;
    }
}
