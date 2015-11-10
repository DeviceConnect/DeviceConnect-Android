/*
 KadecotServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;

/**
 * Kadecot Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public KadecotServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        return ((KadecotDeviceService) getContext()).searchDevice(response);
    }

}
