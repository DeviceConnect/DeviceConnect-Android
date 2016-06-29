/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.service;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingKeyEventProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingLightProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingNotificationProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingProximityProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingVibrationProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;

public class LinkingDeviceService extends DConnectService {

    private LinkingDevice mDevice;

    public LinkingDeviceService(final DConnectMessageService service, final LinkingDevice device) {
        super(device.getBdAddress());

        setName(device.getDisplayName());
        setNetworkType(NetworkType.BLE);

        mDevice = device;

        addProfile(new LinkingDeviceOrientationProfile(service));
        addProfile(new LinkingKeyEventProfile(service));
        addProfile(new LinkingLightProfile());
        addProfile(new LinkingNotificationProfile());
        addProfile(new LinkingProximityProfile(service));
        addProfile(new LinkingVibrationProfile());
    }

    @Override
    public boolean isOnline() {
        return mDevice.isConnected();
    }

    public void setLinkingDevice(final LinkingDevice device) {
        mDevice = device;
    }

    public LinkingDevice getLinkingDevice() {
        return mDevice;
    }
}
