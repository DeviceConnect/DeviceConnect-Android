package org.deviceconnect.android.deviceplugin.irkit.service;


import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitRemoteControllerProfile;
import org.deviceconnect.android.service.DConnectService;

public class IRKitService extends DConnectService {

    private final String mIp;

    public IRKitService(final IRKitDevice device) {
        super(device.getName());
        mIp = device.getIp();
        setName(device.getName());
        setNetworkType(NetworkType.WIFI);

        addProfile(new IRKitRemoteControllerProfile());
    }

    public String getIp() {
        return mIp;
    }
}
