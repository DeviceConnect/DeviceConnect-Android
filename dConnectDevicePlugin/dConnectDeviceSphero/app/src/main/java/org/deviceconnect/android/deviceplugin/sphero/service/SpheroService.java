/*
 SpheroService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.service;

import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroDriveControllerProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroLightProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * Spheroデバイスを管理する.
 * @author NTT DOCOMO, INC.
 */
public class SpheroService extends DConnectService {

    public SpheroService(final DeviceInfo info) {
        super(info.getDevice().getRobot().getIdentifier());
        setName(info.getDevice().getRobot().getName());
        setNetworkType(NetworkType.BLUETOOTH);
        setOnline(info.getDevice().isConnected());

        addProfile(new SpheroLightProfile());
        addProfile(new SpheroDriveControllerProfile());
        addProfile(new SpheroDeviceOrientationProfile());
        addProfile(new SpheroProfile());
    }
}
