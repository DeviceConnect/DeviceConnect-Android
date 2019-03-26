/*
 AuthorizationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.profile;

import android.content.Intent;

import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.request.CreateClientRequest;
import org.deviceconnect.android.manager.core.request.DConnectRequest;
import org.deviceconnect.android.manager.core.request.DConnectRequestManager;
import org.deviceconnect.android.manager.core.request.GetAccessTokenRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.profile.AuthorizationProfileConstants;

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

    private DConnectRequestManager mRequestManager;
    private DConnectSettings mSettings;
    private LocalOAuth2Main mLocalOAuth2Main;

    public AuthorizationProfile(final DConnectSettings settings,
                                final DConnectRequestManager requestManager,
                                final LocalOAuth2Main localOAuth2Main) {
        mSettings = settings;
        mRequestManager = requestManager;
        mLocalOAuth2Main = localOAuth2Main;

        addApi(mGetCreateClient);
        addApi(mGetRequestAccessToken);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     */
    private final DConnectApi mGetCreateClient = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_GRANT;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Local OAuthを使用しない場合にはNot Supportを返却する
            if (!usesLocalOAuth()) {
                MessageUtils.setNotSupportProfileError(response);
                return true;
            }

            DConnectRequest req = new CreateClientRequest(mLocalOAuth2Main);
            req.setContext(getContext());
            req.setRequest(request);
            req.setResponse(response);
            req.setOnResponseCallback((resp) -> sendResponse(resp));
            mRequestManager.addRequest(req);
            return false;
        }
    };

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     */
    private final DConnectApi mGetRequestAccessToken = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ACCESS_TOKEN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Local OAuthを使用しない場合にはNot Supportを返却する
            if (!usesLocalOAuth()) {
                MessageUtils.setNotSupportProfileError(response);
                return true;
            }

            DConnectRequest req = new GetAccessTokenRequest(mLocalOAuth2Main, mSettings.getKeyword());
            req.setContext(getContext());
            req.setRequest(request);
            req.setResponse(response);
            req.setOnResponseCallback((resp) -> sendResponse(resp));
            mRequestManager.addRequest(req);
            return false;
        }
    };

    /**
     * LocalOAuth の有効・無効を確認します.
     *
     * @return LocalOAuthが有効の場合はtrue、それ以外はfalse
     */
    private boolean usesLocalOAuth() {
        return mSettings.isUseALocalOAuth();
    }

    /**
     * 不正なオリジンをもつアプリケーションからリクエストを受信した場合のハンドラー.
     * <p>
     * 本クラスの外部でオリジンの正当性をチェックすること.
     * 不正な場合は本メソッドを呼び出した後、レスポンスを送信すること.
     * </p>
     * @param request リクエスト
     * @param response レスポンス
     */
    public void onInvalidOrigin(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_GRANT.equalsIgnoreCase(attribute)) {
            // GotAPI対応: エラーの場合は、空文字のクライアントIDを返す
            response.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, "");
        } else if (ATTRIBUTE_ACCESS_TOKEN.equalsIgnoreCase(attribute)) {
            // GotAPI対応: エラーの場合は、空文字のアクセストークンIDを返す
            response.putExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN, "");
        }
    }
}
