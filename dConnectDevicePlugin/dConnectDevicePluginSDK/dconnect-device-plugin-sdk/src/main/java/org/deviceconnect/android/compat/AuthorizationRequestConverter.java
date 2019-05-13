package org.deviceconnect.android.compat;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;

/**
 * 旧 Authorization のリクエストを新 Authorization のリクエストに変換するクラス.
 *
 * クライアント作成要求
 *   /authorization/createClient -> /authorization/grant
 *
 * アクセストークン作成要求
 *   /authorization/requestAccessToken -> /authorization/accessToken

 * @author NTT DOCOMO, INC.
 */
public class AuthorizationRequestConverter implements MessageConverter {

    /**
     * プラグイン側のAuthorizationのアトリビュート名: {@value}.
     */
    private static final String ATTRIBUTE_CREATE_CLIENT = "createClient";

    /**
     * プラグイン側のAuthorizationのアトリビュート名: {@value}.
     */
    private static final String ATTRIBUTE_REQUEST_ACCESS_TOKEN = "requestAccessToken";

    @Override
    public void convert(final Intent request) {
        String profileName = DConnectProfile.getProfile(request);
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
    }
}
