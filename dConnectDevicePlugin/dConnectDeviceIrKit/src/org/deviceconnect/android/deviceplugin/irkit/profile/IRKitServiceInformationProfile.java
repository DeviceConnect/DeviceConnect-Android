/*
 IRKitServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * IRKit Service Information Profile.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServiceInformationProfile extends ServiceInformationProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider Provider
     */
    public IRKitServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        
        IRKitDeviceService service = (IRKitDeviceService) getContext();
        IRKitDevice device = service.getDevice(serviceId);
        
        if (device != null) {
            return ConnectState.ON;
        } else {
            return ConnectState.OFF;
        }
    }

}
