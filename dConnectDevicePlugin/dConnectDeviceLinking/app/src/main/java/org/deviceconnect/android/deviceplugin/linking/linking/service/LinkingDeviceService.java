/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.service;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingBatteryProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingHumidityProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingKeyEventProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingLightProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingNotificationProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingProximityProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingTemperatureProfile;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingVibrationProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.service.DConnectService;

public class LinkingDeviceService extends DConnectService {

    private LinkingDevice mDevice;

    public LinkingDeviceService(final DConnectMessageService service, final LinkingDevice device) {
        super(device.getBdAddress());

        setName(device.getDisplayName());
        setNetworkType(NetworkType.BLE);

        mDevice = device;

        if (mDevice.isGyro() || mDevice.isAcceleration() || mDevice.isCompass()) {
            addProfile(new LinkingDeviceOrientationProfile(service));
        }
        if (mDevice.isButton()) {
            addProfile(new LinkingKeyEventProfile(service));
        }
        if (mDevice.isLED()) {
            addProfile(new LinkingLightProfile());
        }
        addProfile(new LinkingNotificationProfile());
        addProfile(new LinkingProximityProfile(service));
        if (mDevice.isVibration()) {
            addProfile(new LinkingVibrationProfile());
        }
        if (mDevice.isBattery()) {
            addProfile(new LinkingBatteryProfile(service));
        }
        if (mDevice.isTemperature()) {
            addProfile(new LinkingTemperatureProfile());
        }
        if (mDevice.isHumidity()) {
            addProfile(new LinkingHumidityProfile());
        }
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

    public void destroy() {
        for (DConnectProfile profile : getProfileList()) {
            if (profile instanceof LinkingBatteryProfile) {
                ((LinkingBatteryProfile) profile).destroy();
            } else if (profile instanceof LinkingDeviceOrientationProfile) {
                ((LinkingDeviceOrientationProfile) profile).destroy();
            } else if (profile instanceof LinkingKeyEventProfile) {
                ((LinkingKeyEventProfile) profile).destroy();
            } else if (profile instanceof LinkingProximityProfile) {
                ((LinkingProximityProfile) profile).destroy();
            }
        }
    }
}
