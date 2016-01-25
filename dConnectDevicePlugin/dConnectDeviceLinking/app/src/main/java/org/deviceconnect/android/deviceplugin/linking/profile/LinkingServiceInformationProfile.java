/*
 LinkingServiceInformationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * Linking Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingServiceInformationProfile extends ServiceInformationProfile {

    public LinkingServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getBluetoothState(String serviceId) {
        if (serviceId == null || serviceId.length() == 0) {
            return ConnectState.OFF;
        }
        LinkingDevice device = LinkingUtil.getLinkingDevice(getContext(), serviceId);
        if (device == null) {
            return ConnectState.OFF;
        }
        return device.isConnected() ? ConnectState.ON : ConnectState.OFF;
    }

}
