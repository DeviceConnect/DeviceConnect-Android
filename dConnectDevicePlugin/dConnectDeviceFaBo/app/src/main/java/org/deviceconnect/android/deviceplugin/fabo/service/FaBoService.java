package org.deviceconnect.android.deviceplugin.fabo.service;


import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoDriveControllerProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.service.DConnectService;

public class FaBoService extends DConnectService {
    private static final String SERVICE_ID = "gpio_service_id";
    private static final String DEVICE_NAME = "FaBo Device";

    public FaBoService() {
        super(SERVICE_ID);
        setName(DEVICE_NAME);
        setNetworkType(NetworkType.UNKNOWN);
        setOnline(true);
        addProfile(new FaBoGPIOProfile());
        addProfile(new FaBoDriveControllerProfile());
    }
}
