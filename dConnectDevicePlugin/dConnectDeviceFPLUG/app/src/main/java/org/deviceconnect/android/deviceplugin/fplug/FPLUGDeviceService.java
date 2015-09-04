/*
 FPLUGDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug;

import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGHumidityProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGIlluminanceProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGLightProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGPowerMeterProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGSettingsProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGSystemProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGTemperatureProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * F-PLUG device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        addProfile(new FPLUGPowerMeterProfile());
        addProfile(new FPLUGLightProfile());
        addProfile(new FPLUGSettingsProfile());
        addProfile(new FPLUGTemperatureProfile());
        addProfile(new FPLUGHumidityProfile());
        addProfile(new FPLUGIlluminanceProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new FPLUGSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new FPLUGServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new FPLUGServiceDiscoveryProfile(this);
    }

}
