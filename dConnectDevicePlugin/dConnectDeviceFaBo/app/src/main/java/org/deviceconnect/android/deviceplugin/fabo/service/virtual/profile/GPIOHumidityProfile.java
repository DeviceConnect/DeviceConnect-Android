package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;

import java.util.List;

public class GPIOHumidityProfile extends BaseFaBoProfile {
    /**
     * Humidity操作を行うピンのリスト.
     */
    private List<ArduinoUno.Pin> mPinList;

    public GPIOHumidityProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;
    }

    @Override
    public String getProfileName() {
        return "humidity";
    }
}
