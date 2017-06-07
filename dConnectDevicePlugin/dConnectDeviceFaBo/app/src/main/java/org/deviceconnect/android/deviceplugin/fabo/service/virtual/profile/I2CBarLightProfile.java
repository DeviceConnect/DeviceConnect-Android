package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Lightプロファイル.
 * <p>
 * ID: #403<br>
 * Name: ColorLED Bar Brick<br>
 * </p>
 */
public class I2CBarLightProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "light";
    }
}
