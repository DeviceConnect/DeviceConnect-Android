package org.deviceconnect.android.deviceplugin.linking.linking.service;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.profile.LinkingLightProfile;
import org.deviceconnect.android.service.DConnectService;

public class LinkingDeviceService extends DConnectService {

    private LinkingDevice mDevice;

    public LinkingDeviceService(final LinkingDevice device) {
        super(device.getBdAddress());
        mDevice = device;

        addProfile(new LinkingLightProfile());
    }

    public LinkingDevice getLinkingDevice() {
        return mDevice;
    }
}
