/*
 GetAccessTokenRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.localoauth.AccessTokenData;
import org.deviceconnect.android.localoauth.AccessTokenScope;
import org.deviceconnect.android.localoauth.ConfirmAuthParams;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.PublishAccessTokenListener;
import org.deviceconnect.android.localoauth.exception.AuthorizatonException;
import org.deviceconnect.android.manager.profile.AuthorizationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.profile.AuthorizationProfileConstants;

import android.content.Intent;
import android.os.Bundle;

/**
 * LocalOAuthにアクセストークンを要求するリクエスト.
 * @author NTT DOCOMO, INC.
 */
public class GetAccessTokenRequest extends DConnectRequest {

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    @Override
    public boolean hasRequestCode(final int requestCode) {
        return false;
    }

    @Override
    public void run() {
        try {
            getAccessToken();
        } catch (AuthorizatonException e) {
            setAuthorizationError(mResponse, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            setInvalidRequestParameterError(mResponse, e.getMessage());
        } catch (IllegalArgumentException e) {
            setInvalidRequestParameterError(mResponse, e.getMessage());
        } catch (IllegalStateException e) {
            setInvalidRequestParameterError(mResponse, e.getMessage());
        }
        sendResponse(mResponse);
    }

    /**
     * アクセストークンの取得の処理を行う.
     * @throws AuthorizatonException 認証に失敗した場合に発生
     * @throws UnsupportedEncodingException 文字のエンコードに失敗した場合に発生
     */
    private void getAccessToken() throws AuthorizatonException, UnsupportedEncodingException {
        String serviceId = mRequest.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String clientId = mRequest.getStringExtra(AuthorizationProfile.PARAM_CLIENT_ID);
        String[] scopes = parseScopes(mRequest.getStringExtra(AuthorizationProfile.PARAM_SCOPE));
        String applicationName = mRequest.getStringExtra(AuthorizationProfile.PARAM_APPLICATION_NAME);

        // TODO _typeからアプリorデバイスプラグインかを判別できる？
        ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                .clientId(clientId).scopes(scopes).applicationName(applicationName)
                .isForDevicePlugin(false) 
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

        if (token[0] != null && token[0].getAccessToken() != null) {
            mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            mResponse.putExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN, token[0].getAccessToken());
            mResponse.putExtra(DConnectMessage.EXTRA_ERROR_CODE, 0);
            mResponse.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, "");
            AccessTokenScope[] atScopes = token[0].getScopes();
            if (atScopes != null) {
                List<Bundle> s = new ArrayList<Bundle>();
                for (int i = 0; i < atScopes.length; i++) {
                    Bundle b = new Bundle();
                    b.putString(AuthorizationProfileConstants.PARAM_SCOPE,
                            atScopes[i].getScope());
                    b.putLong(AuthorizationProfileConstants.PARAM_EXPIRE_PERIOD,
                            atScopes[i].getExpirePeriod());
                    s.add(b);
                }
                mResponse.putExtra(AuthorizationProfileConstants.PARAM_SCOPES,
                        s.toArray(new Bundle[s.size()]));
            }
        } else {
            setAuthorizationError(mResponse, "Cannot create a access token.");
        }
        sendResponse(mResponse);
    }

    /**
     * レスポンスのエラーコードに 認証エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    private static void setAuthorizationError(final Intent response, final String message) {
        setError(response, ErrorCode.AUTHORIZATION, message);
    }

    /**
     * レスポンスのエラーコードに 不正なパラメータエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    private static void setInvalidRequestParameterError(final Intent response, final String message) {
        setError(response, ErrorCode.INVALID_REQUEST_PARAMETER, message);
    }

    /**
     * レスポンスにエラーを設定する.
     * <p>
     * GotAPI 1.0仕様により、空文字のアクセストークンを設定する.
     * </p>
     * 
     * @param response エラーを設定するレスポンスパラメータ
     * @param error エラーコード
     * @param message エラーメッセージ
     */
    private static void setError(final Intent response, final ErrorCode error, final String message) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        response.putExtra(DConnectMessage.EXTRA_ERROR_CODE, error.getCode());
        response.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, (message == null ? error.toString() : message));
        
        // GotAPI対応: エラーの場合は、空文字のアクセストークンを返す
        response.putExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN, "");
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
