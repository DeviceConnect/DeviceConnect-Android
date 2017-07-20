package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Lightプロファイル.
 * <p>
 * ID: #401<br>
 * Name: ColorLED One Brick<br>
 * </p>
 */
public class I2COneLightProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "light";
    }
}
