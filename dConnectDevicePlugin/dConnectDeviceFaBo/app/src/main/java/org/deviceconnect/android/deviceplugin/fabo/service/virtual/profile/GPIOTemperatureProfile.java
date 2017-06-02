package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.profile.DConnectProfile;

import java.util.List;

/**
 * GPIO用のTemperatureプロファイル.
 */
public class GPIOTemperatureProfile extends DConnectProfile {
    private List<ArduinoUno.Pin> mPinList;

    public GPIOTemperatureProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;
    }

    @Override
    public String getProfileName() {
        return "temperature";
    }
}
