package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Lightプロファイル.
 * <p>
 * ID: #404<br>
 * Name: ColorLED Matrix Brick<br>
 * </p>
 */
public class I2CMatrixLightProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "light";
    }
}
