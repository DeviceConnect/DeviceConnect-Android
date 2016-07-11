package org.deviceconnect.android.compat;


import android.content.Intent;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;

public class LowerCaseConverter implements MessageConverter {

    @Override
    public void convert(final Intent request) {
        String profileName = DConnectProfile.getProfile(request);
        String interfaceName = DConnectProfile.getInterface(request);
        String attributeName = DConnectProfile.getAttribute(request);

        if (isAccessTokenRequest(profileName, attributeName)) {
            String scope = request.getStringExtra(AuthorizationProfile.PARAM_SCOPE);
            if (scope != null) {
                request.putExtra(AuthorizationProfile.PARAM_SCOPE, scope.toLowerCase());
            }
        }

        if (profileName != null) {
            DConnectProfile.setProfile(request, profileName.toLowerCase());
        }
        if (interfaceName != null) {
            DConnectProfile.setInterface(request, interfaceName.toLowerCase());
        }
        if (attributeName != null) {
            DConnectProfile.setAttribute(request, attributeName.toLowerCase());
        }
    }

    private boolean isAccessTokenRequest(final String profileName, final String attributeName) {
        return AuthorizationProfile.PROFILE_NAME.equalsIgnoreCase(profileName)
               && AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN.equalsIgnoreCase(attributeName);
    }

}
