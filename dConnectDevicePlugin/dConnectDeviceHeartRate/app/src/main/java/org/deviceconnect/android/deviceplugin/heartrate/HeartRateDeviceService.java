/*
 HeartRateDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();

        HeartRateApplication app = (HeartRateApplication) getApplication();
        app.initialize();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HeartRateSystemProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HeartRateServiceDiscoveryProfile();
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateApplication app = (HeartRateApplication) getApplication();
        return app.getHeartRateManager();
    }
}
