package org.deviceconnect.android.deviceplugin.awsiot.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.remote.AWSIotRemoteManager;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;

public class AWSIotServiceDiscoveryProfile extends ServiceDiscoveryProfile {
    public AWSIotServiceDiscoveryProfile(final AWSIotRemoteManager mgr, final DConnectServiceProvider provider) {
        super(provider);

        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return mgr.sendServiceDiscovery(request, response);
            }
        });
    }
}
