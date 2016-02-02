/*
 AuthorizationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.request.CreateClientRequest;
import org.deviceconnect.android.manager.request.DConnectRequest;
import org.deviceconnect.android.manager.request.GetAccessTokenRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.profile.AuthorizationProfileConstants;

import android.content.Intent;

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

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        // Local OAuthを使用しない場合にはNot Supportを返却する
        DConnectSettings settings = DConnectSettings.getInstance();
        if (!settings.isUseALocalOAuth()) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        String attribute = getAttribute(request);
        if (ATTRIBUTE_GRANT.equals(attribute)) {
            onGetCreateClient(request, response);
        } else if (ATTRIBUTE_ACCESS_TOKEN.equals(attribute)) {
            onGetRequestAccessToken(request, response);
        } else {
            sendUnknownAttributeError(request, response);
        }

        // 各デバイスプラグインに送信する場合にはfalseを返却、
        // dConnectManagerで止める場合にはtrueを返却する
        // ここでは、各デバイスには渡さないのでtrueを返却する。
        return true;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
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
        if (ATTRIBUTE_GRANT.equals(attribute)) {
            // GotAPI対応: エラーの場合は、空文字のクライアントIDを返す
            response.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, "");
        } else if (ATTRIBUTE_ACCESS_TOKEN.equals(attribute)) {
            // GotAPI対応: エラーの場合は、空文字のアクセストークンIDを返す
            response.putExtra(AuthorizationProfile.PARAM_ACCESS_TOKEN, "");
        }
    }

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     * 
     * 各デバイスプラグインに送信する場合にはtrueを返却、
     * dConnectManagerで止める場合にはfalseを返却する
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * 
     * @return 各デバイスプラグインに送信する場合にはtrue、dConnectManagerで止める場合にはfalseを返却する
     */
    private boolean onGetCreateClient(final Intent request, final Intent response) {
        DConnectRequest req = new CreateClientRequest();
        req.setContext(getContext());
        req.setRequest(request);
        req.setResponse(response);
        ((DConnectMessageService) getContext()).addRequest(req);
        return true;
    }

    /**
     * Local OAuthで使用するクライアントを作成要求を行う.
     * 
     * 各デバイスプラグインに送信する場合にはtrueを返却、
     * dConnectManagerで止める場合にはfalseを返却する
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * 
     * @return 各デバイスプラグインに送信する場合にはtrue、dConnectManagerで止める場合にはfalseを返却する
     */
    private boolean onGetRequestAccessToken(final Intent request, final Intent response) {
        DConnectRequest req = new GetAccessTokenRequest();
        req.setContext(getContext());
        req.setRequest(request);
        req.setResponse(response);
        ((DConnectMessageService) getContext()).addRequest(req);
        return true;
    }

    /**
     * Authorizationで定義されていないattributeが指定されていたときのエラーを返却する.
     * @param request リクエスト
     * @param response レスポンス
     */
    private void sendUnknownAttributeError(final Intent request, final Intent response) {
        MessageUtils.setUnknownAttributeError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
    }
}
