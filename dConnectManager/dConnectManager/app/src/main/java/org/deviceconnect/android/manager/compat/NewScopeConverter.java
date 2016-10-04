package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;

import java.util.ArrayList;
import java.util.List;


/**
 * アクセストークン要求のスコープを新仕様に統一するクラス.
 * @author NTT DOCOMO, INC.
 */
public class NewScopeConverter implements MessageConverter {

    @Override
    public boolean convert(final Intent request) {
        String profileName = DConnectProfile.getProfile(request);
        String attributeName = DConnectProfile.getAttribute(request);
        if (!isAccessTokenRequest(profileName, attributeName)) {
            return false;
        }
        String scopeParam = request.getStringExtra(AuthorizationProfile.PARAM_SCOPE);
        if (scopeParam == null) {
            return false;
        }
        String[] scopes = scopeParam.split(",");
        List<String> forwardScopes = new ArrayList<String>();
        for (String scope : scopes) {
            String forwardScope = PathConversionTable.forwardProfileName(scope);
            forwardScopes.add(forwardScope);
        }
        request.putExtra(AuthorizationProfile.PARAM_SCOPE, concat(forwardScopes));
        return true;
    }

    private String concat(final List<String> list) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(list.get(i));
        }
        return result.toString();
    }

    private boolean isAccessTokenRequest(final String profileName, final String attributeName) {
        return AuthorizationProfile.PROFILE_NAME.equalsIgnoreCase(profileName)
            && AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN.equalsIgnoreCase(attributeName);
    }

}
