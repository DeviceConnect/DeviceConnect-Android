package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Illuminaceプロファイル.
 * <p>
 * ID #217<br>
 * Name: Ambient Light I2C Brick<br>
 * </p>
 */
public class I2CIlluminanceProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "illuminance";
    }
}
