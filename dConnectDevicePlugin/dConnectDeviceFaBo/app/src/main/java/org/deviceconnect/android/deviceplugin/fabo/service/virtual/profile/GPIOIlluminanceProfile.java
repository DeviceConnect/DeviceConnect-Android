package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * GPIO用のIlluminanceプロファイル.
 * <p>
 *  ID: #109<br>
 *  Name: Light Brick<br>
 * </p>
 */
public class GPIOIlluminanceProfile extends BaseFaBoProfile {
    /**
     * Illuminance操作を行うピンのリスト.
     */
    private List<FaBoShield.Pin> mPinList;

    public GPIOIlluminanceProfile(final List<FaBoShield.Pin> pinList) {
        mPinList = pinList;

        // GET /gotpai/illuminance
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else {
                    FaBoShield.Pin pin = mPinList.get(0);

                    int value = getFaBoDeviceControl().getAnalog(pin);
                    value = 5000 - calcArduinoMap(value, 0, 2013, 0, 5000);
                    value = value / 10;

                    response.putExtra("illuminance", value);

                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "illuminance";
    }
}
