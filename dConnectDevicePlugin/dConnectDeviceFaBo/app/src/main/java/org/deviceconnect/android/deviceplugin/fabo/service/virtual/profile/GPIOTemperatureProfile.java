package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * GPIO用のTemperatureプロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #108<br>
 * Name: Temperature Brick<br>
 * </p>
 */
public class GPIOTemperatureProfile extends BaseFaBoProfile {
    /**
     * Temperatureを取得するピン.
     */
    private List<ArduinoUno.Pin> mPinList;

    /**
     * コンストラクタ.
     * @param pinList 操作を行うピンのリスト
     */
    public GPIOTemperatureProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;

        // GET /gotpai/temperature
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else {
                    ArduinoUno.Pin pin = mPinList.get(0);

                    int value = getFaBoDeviceControl().getAnalog(pin);
                    value = calcArduinoMap(value, 0, 1023, 0, 5000);
                    value = calcArduinoMap(value, 300, 1600, -30, 100);
                    value = Math.round(value * 10) / 10;

                    response.putExtra("temperature", value);

                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "temperature";
    }
}
