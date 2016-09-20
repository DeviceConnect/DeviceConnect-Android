package org.deviceconnect.android.deviceplugin.hue.service;


import com.philips.lighting.hue.sdk.PHAccessPoint;

import org.deviceconnect.android.deviceplugin.hue.profile.HueLightProfile;
import org.deviceconnect.android.service.DConnectService;

public class HueService extends DConnectService {

    private static final String NAME_PREFIX = "hue ";

    public HueService(final PHAccessPoint accessPoint) {
        super(accessPoint.getIpAddress());
        setName(NAME_PREFIX + accessPoint.getMacAddress());
        setNetworkType(NetworkType.WIFI);
        addProfile(new HueLightProfile());
    }

}
