/*
 FPLUGService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.service;

import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGHumidityProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGIlluminanceProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGLightProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGPowerMeterProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGSettingProfile;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGTemperatureProfile;
import org.deviceconnect.android.service.DConnectService;


public class FPLUGService extends DConnectService {

    private static final String NAME_PREFIX = "FPLUG:";

    public FPLUGService(final String id) {
        super(id);
        setName(NAME_PREFIX + id);
        setNetworkType(NetworkType.BLUETOOTH);
        addProfile(new FPLUGPowerMeterProfile());
        addProfile(new FPLUGLightProfile());
        addProfile(new FPLUGSettingProfile());
        addProfile(new FPLUGTemperatureProfile());
        addProfile(new FPLUGHumidityProfile());
        addProfile(new FPLUGIlluminanceProfile());
    }

}
