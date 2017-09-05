/*
 AuthorizationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import android.content.Intent;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.request.CreateClientRequest;
import org.deviceconnect.android.manager.request.DConnectRequest;
import org.deviceconnect.android.manager.request.GetAccessTokenRequest;
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

    public AuthorizationProfile() {
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

            DConnectRequest req = new CreateClientRequest();
            req.setContext(getContext());
            req.setRequest(request);
            req.setResponse(response);
            ((DConnectMessageService) getContext()).addRequest(req);
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

            DConnectRequest req = new GetAccessTokenRequest(getSettings().getKeyword());
            req.setContext(getContext());
            req.setRequest(request);
            req.setResponse(response);
            ((DConnectMessageService) getContext()).addRequest(req);
            return false;
        }
    };

    private DConnectSettings getSettings() {
        return ((DConnectMessageService) getContext()).getSettings();
    }

    private boolean usesLocalOAuth() {
        return getSettings().isUseALocalOAuth();
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
