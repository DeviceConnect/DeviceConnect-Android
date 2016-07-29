package org.deviceconnect.android.deviceplugin.irkit.service;


import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitRemoteControllerProfile;
import org.deviceconnect.android.service.DConnectService;

public class IRKitService extends DConnectService {

    public IRKitService(final IRKitDevice device) {
        super(device.getName());
        setName(device.getName());
        setNetworkType(NetworkType.WIFI);

        addProfile(new IRKitRemoteControllerProfile());
    }
}
