package org.deviceconnect.android.deviceplugin.hvcp.service;


import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcp.profile.HVCPHumanDetectionProfile;
import org.deviceconnect.android.service.DConnectService;

public class HVCPService extends DConnectService {

    private static final String NETWORK_TYPE_USB = "usb";

    public HVCPService(final HVCCameraInfo cameraInfo) {
        super(cameraInfo.getID());
        setName(cameraInfo.getName());
        setNetworkType(NETWORK_TYPE_USB);

        addProfile(new HVCPHumanDetectionProfile());
    }

}
