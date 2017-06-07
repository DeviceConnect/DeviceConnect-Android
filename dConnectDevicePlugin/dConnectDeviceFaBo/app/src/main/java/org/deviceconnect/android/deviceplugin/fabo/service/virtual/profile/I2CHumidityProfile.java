package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Humidityプロファイル.
 * <p>
 * ID: #208<br>
 * Name: Humidity I2C Brick<br>
 * </p>
 */
public class I2CHumidityProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "humidity";
    }
}
