/*
 FPLUGServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    public FPLUGServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<FPLUGController> fplugs = ((FPLUGApplication) getContext().getApplicationContext()).getConnectedController();
        if (fplugs.size() == 0) {
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
        Bundle[] services = new Bundle[fplugs.size()];
        int index = 0;
        for (FPLUGController fplug : fplugs) {
            Bundle service = new Bundle();
            ServiceDiscoveryProfile.setId(service, fplug.getAddress());
            ServiceDiscoveryProfile.setName(service, "F-PLUG:" + fplug.getAddress());
            ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
            ServiceDiscoveryProfile.setOnline(service, true);
            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
            services[index] = service;
            index++;
        }
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
