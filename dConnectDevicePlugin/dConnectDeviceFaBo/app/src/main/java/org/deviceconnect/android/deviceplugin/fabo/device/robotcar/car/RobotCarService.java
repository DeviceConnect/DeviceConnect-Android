package org.deviceconnect.android.deviceplugin.fabo.device.robotcar.car;

import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.car.profile.RobotCarDriveControllerProfile;
import org.deviceconnect.android.service.DConnectService;

public class RobotCarService extends DConnectService {
    private static final String SERVICE_ID = "car_service_id";
    private static final String DEVICE_NAME = "Robot Car (Car)";

    public RobotCarService() {
        super(SERVICE_ID);
        setName(DEVICE_NAME);
        setNetworkType(NetworkType.UNKNOWN);
        setOnline(true);
        addProfile(new RobotCarDriveControllerProfile());
    }
}
