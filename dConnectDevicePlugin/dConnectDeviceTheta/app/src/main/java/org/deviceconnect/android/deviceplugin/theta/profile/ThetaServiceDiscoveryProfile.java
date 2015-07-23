/*
 ThetaServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;

/**
 * Theta Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public ThetaServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        return ((ThetaDeviceService) getContext()).searchDevice(request, response);
    }

}
