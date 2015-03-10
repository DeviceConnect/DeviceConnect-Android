/*
 CreateClientRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import org.deviceconnect.android.localoauth.ClientData;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.exception.AuthorizatonException;
import org.deviceconnect.android.manager.profile.AuthorizationProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.restlet.ext.oauth.PackageInfoOAuth;

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
        if (origin == null) {
            MessageUtils.setInvalidRequestParameterError(mResponse);
        } else {
            // Local OAuthでクライアント作成
            PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
            try {
                ClientData client = LocalOAuth2Main.createClient(packageInfo);
                if (client != null) {
                    mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    mResponse.putExtra(AuthorizationProfile.PARAM_CLIENT_ID, client.getClientId());
                } else {
                    MessageUtils.setAuthorizationError(mResponse);
                }
            } catch (AuthorizatonException e) {
                MessageUtils.setAuthorizationError(mResponse, e.getMessage());
            } catch (IllegalArgumentException e) {
                MessageUtils.setInvalidRequestParameterError(mResponse, e.getMessage());
            }
        }
        sendResponse(mResponse);
    }
}
