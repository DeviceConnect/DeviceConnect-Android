package org.deviceconnect.android.compat;


import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

public class ServiceDiscoveryRequestConverter implements MessageConverter {

    /** プラグイン側のService Discoveryのプロファイル名: {@value}. */
    private static final String PROFILE_NETWORK_SERVICE_DISCOVERY = "networkServiceDiscovery";

    /** プラグイン側のService Discoveryのアトリビュート名: {@value}. */
    private static final String ATTRIBUTE_GET_NETWORK_SERVICES = "getNetworkServices";

    @Override
    public void convert(final Intent request) {
        String profileName = DConnectProfile.getProfile(request);
        if (PROFILE_NETWORK_SERVICE_DISCOVERY.equals(profileName)) {
            profileName = ServiceDiscoveryProfileConstants.PROFILE_NAME;
            String attributeName = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
            if (ATTRIBUTE_GET_NETWORK_SERVICES.equals(attributeName)) {
                request.putExtra(DConnectMessage.EXTRA_PROFILE, profileName);
                request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, (String) null);
            }
        }
    }
}
