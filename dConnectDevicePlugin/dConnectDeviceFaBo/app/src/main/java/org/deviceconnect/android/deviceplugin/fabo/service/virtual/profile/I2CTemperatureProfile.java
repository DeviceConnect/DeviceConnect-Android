package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;

/**
 * I2C用Temperatureプロファイル.
 * <p>
 * ID: #207<br>
 * Name: Temperature I2C Brick<br>
 * </p>
 */
public class I2CTemperatureProfile extends BaseFaBoProfile {
    /**
     * コンストラクタ.
     */
    public I2CTemperatureProfile() {
        // GET /gotpai/temperature
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportProfileError(response, "Not implements yet.");
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "temperature";
    }
}
