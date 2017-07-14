package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * GPIO用Humidityプロファイル.
 * <p>
 *  ID: #115<br>
 *  Name: Humidity Brick<br>
 * </p>
 */
public class GPIOHumidityProfile extends BaseFaBoProfile {
    /**
     * Humidity操作を行うピンのリスト.
     */
    private List<FaBoShield.Pin> mPinList;

    public GPIOHumidityProfile(final List<FaBoShield.Pin> pinList) {
        mPinList = pinList;

        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else {
                    FaBoShield.Pin pin = mPinList.get(0);

                    // TODO 要確認
                    int value = getFaBoDeviceControl().getAnalog(pin);
                    int humidity = value & 0xFFFF;

                    response.putExtra("humidity", humidity / 100.0f);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "humidity";
    }
}
