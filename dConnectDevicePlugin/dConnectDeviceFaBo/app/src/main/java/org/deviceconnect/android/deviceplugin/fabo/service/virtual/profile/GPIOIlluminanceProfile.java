package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * GPIO用のIlluminanceプロファイル.
 */
public class GPIOIlluminanceProfile extends BaseFaBoProfile {
    /**
     * Illuminance操作を行うピンのリスト.
     */
    private List<ArduinoUno.Pin> mPinList;

    public GPIOIlluminanceProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;

        // GET /gotpai/illuminance
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                ArduinoUno.Pin pin = mPinList.get(0);

                int value = getFaBoDeviceService().getAnalogValue(pin);
                value = 5000 - 5000 * value / 1023;
                value = value / 10;
                response.putExtra("illuminance", value);

                setResult(response,  DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "illuminance";
    }
}
