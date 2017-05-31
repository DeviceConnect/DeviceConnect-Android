package org.deviceconnect.android.deviceplugin.fabo.device.robotcar.mouse;

import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.mouse.profile.MouseCarDriveControllerProfile;
import org.deviceconnect.android.service.DConnectService;

public class MouseCarService extends DConnectService {
    private static final String SERVICE_ID = "mouse_service_id";
    private static final String DEVICE_NAME = "Robot Car (Mouse)";

    public MouseCarService() {
        super(SERVICE_ID);
        setName(DEVICE_NAME);
        setNetworkType(NetworkType.UNKNOWN);
        setOnline(true);
        addProfile(new MouseCarDriveControllerProfile());
    }
}
