/*
 FPLUGServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * F-PLUG Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGServiceInformationProfile extends ServiceInformationProfile {

    public FPLUGServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getBluetoothState(String serviceId) {
        return isConnected(serviceId) ? ConnectState.ON : ConnectState.OFF;
    }

    private boolean isConnected(String address) {
        return ((FPLUGApplication) getContext().getApplicationContext()).isConnectedFPlug(address);
    }

}
