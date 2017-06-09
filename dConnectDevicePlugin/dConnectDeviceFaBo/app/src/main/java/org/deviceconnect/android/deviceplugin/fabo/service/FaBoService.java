package org.deviceconnect.android.deviceplugin.fabo.service;

import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * FaBoを直接操作するためのサービス.
 */
public class FaBoService extends DConnectService {
    /**
     * サービスを識別するID.
     */
    private static final String SERVICE_ID = "gpio_service_id";

    /**
     * サービス名.
     */
    private static final String DEVICE_NAME = "FaBo Device";

    /**
     * コンストラクタ.
     */
    public FaBoService() {
        super(SERVICE_ID);
        setName(DEVICE_NAME);
        setNetworkType(NetworkType.UNKNOWN);
        setOnline(true);
        addProfile(new FaBoGPIOProfile());
        addProfile(new FaBoProfile());
    }
}
