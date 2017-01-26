/*
 AWSIotServiceDiscoveryProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;

public class AWSIotServiceDiscoveryProfile extends ServiceDiscoveryProfile {
    public AWSIotServiceDiscoveryProfile(final AWSIotDeviceService service, final DConnectServiceProvider provider) {
        super(provider);

        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return service.getAWSIotRemoteManager().sendServiceDiscovery(request, response);
            }
        });
    }
}
