/*
 LinkingServiceDiscoveryProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

public class LinkingServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private static final int TIMEOUT = 20 * 1000;

    private LinkingDevicePluginService mService;

    public LinkingServiceDiscoveryProfile(final LinkingDevicePluginService service, final DConnectServiceProvider provider) {
        super(provider);
        mService = service;
        addApi(mServiceDiscoveryApi);
    }

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            mgr.startBeaconScanWithTimeout(TIMEOUT);

            mService.refreshDevices();

            List<Bundle> serviceBundles = new ArrayList<>();
            for (DConnectService service : getServiceProvider().getServiceList()) {
                Bundle serviceBundle = new Bundle();
                setId(serviceBundle, service.getId());
                setType(serviceBundle, service.getNetworkType());
                setName(serviceBundle, service.getName());
                setOnline(serviceBundle, service.isOnline());
                if (service.getConfig() != null) {
                    setConfig(serviceBundle, service.getConfig());
                }
                setScopes(serviceBundle, service);
                serviceBundles.add(serviceBundle);
            }
            setServices(response, serviceBundles);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
