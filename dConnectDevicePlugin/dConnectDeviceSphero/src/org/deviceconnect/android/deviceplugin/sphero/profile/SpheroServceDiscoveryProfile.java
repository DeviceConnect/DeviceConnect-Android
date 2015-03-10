/*
 SpheroServceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import java.util.Collection;

import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * NetworkServiceDiscovery Profile.
 * @author NTT DOCOMO, INC.
 */
public class SpheroServceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     * 
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public SpheroServceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {

        Collection<DeviceInfo> devices = SpheroManager.INSTANCE.getConnectedDevices();

        Bundle[] services = new Bundle[devices.size()];
        int index = 0;
        for (DeviceInfo info : devices) {

            Bundle service = new Bundle();
            ServiceDiscoveryProfile.setId(service, info.getDevice().getUniqueId());
            ServiceDiscoveryProfile.setName(service, info.getDevice().getName());
            ServiceDiscoveryProfile.setType(service, NetworkType.BLUETOOTH);
            ServiceDiscoveryProfile.setOnline(service, true);
            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());

            services[index++] = service;
        }

        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);

        return true;
    }
}
