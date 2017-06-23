package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;

/**
 * I2C用Humidityプロファイル.
 * <p>
 * ID: #208<br>
 * Name: Humidity I2C Brick<br>
 * </p>
 */
public class I2CHumidityProfile extends BaseFaBoProfile {

    public I2CHumidityProfile() {
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setUnknownError(response);
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "humidity";
    }
}
