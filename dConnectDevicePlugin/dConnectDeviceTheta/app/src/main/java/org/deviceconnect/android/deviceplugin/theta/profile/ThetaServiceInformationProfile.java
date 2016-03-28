/*
 ThetaServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * Theta Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaServiceInformationProfile extends ServiceInformationProfile {

    /**
     * Constructor.
     *
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public ThetaServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        if (ThetaOmnidirectionalImageProfile.SERVICE_ID.equals(serviceId)) {
            return ConnectState.NONE;
        }

        ThetaDeviceManager deviceMgr = ((ThetaDeviceService) getContext()).getDeviceManager();
        ConnectState state;
        if (deviceMgr.getConnectedDeviceById(serviceId) != null) {
            state = ConnectState.ON;
        } else {
            state = ConnectState.OFF;
        }
        return state;
    }

}
