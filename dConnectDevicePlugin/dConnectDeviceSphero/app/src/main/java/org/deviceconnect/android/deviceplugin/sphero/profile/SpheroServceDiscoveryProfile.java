/*
 SpheroServceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.service.DConnectServiceProvider;

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
    public SpheroServceDiscoveryProfile(final DConnectServiceProvider provider) {

        super(provider);
    }

//    @Override
//    protected boolean onGetServices(final Intent request, final Intent response) {
//
//        Collection<DeviceInfo> devices = SpheroManager.INSTANCE.getConnectedDevices();
//
//        Bundle[] services = new Bundle[devices.size()];
//        int index = 0;
//        for (DeviceInfo info : devices) {
//
//            Bundle service = new Bundle();
//            ServiceDiscoveryProfile.setId(service, info.getDevice().getRobot().getIdentifier());
//            ServiceDiscoveryProfile.setName(service, info.getDevice().getRobot().getName());
//            ServiceDiscoveryProfile.setType(service, NetworkType.BLUETOOTH);
//            ServiceDiscoveryProfile.setOnline(service, info.getDevice().isConnected());
//            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
//
//            services[index++] = service;
//        }
//
//        setServices(response, services);
//        setResult(response, DConnectMessage.RESULT_OK);
//
//        return true;
//    }
}
