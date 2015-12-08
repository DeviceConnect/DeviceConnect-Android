/*
 CreateClientRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import org.deviceconnect.android.localoauth.ClientData;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.exception.AuthorizationException;
import org.deviceconnect.android.manager.profile.AuthorizationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.restlet.ext.oauth.PackageInfoOAuth;

import android.content.Intent;

/**
 * LocalOAuth2にクライアントを作成するためのリクエスト.
 * @author NTT DOCOMO, INC.
 */
public class CreateClientRequest extends DConnectRequest {

    @Override
    public boolean hasRequestCode(final int requestCode) {
        return false;
    }

    @Override
    public void run() {
        String origin = mRequest.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);

        // Local OAuthでクライアント作成
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData client = LocalOAuth2Main.createClient(packageInfo);
            if (client != null) {
                mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                mResponse.putExtra(DConnectMessage.EXTRA_ERROR_CODE, 0);
                mResponse.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, "");
                mResponse.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, client.getClientId());
            } else {
                setAuthorizationError(mResponse, null);
            }
        } catch (AuthorizationException e) {
            setAuthorizationError(mResponse, e.getMessage());
        } catch (IllegalArgumentException e) {
            setInvalidRequestParameterError(mResponse, e.getMessage());
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
     * GotAPI 1.0仕様により、空文字のクライアントIDを設定する.
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

        // GotAPI対応: エラーの場合は、空文字のクライアントIDを返す
        response.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, "");
    }
}
