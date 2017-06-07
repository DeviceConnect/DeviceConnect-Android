package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用Proximityプロファイル.
 * <p>
 * ID: #205<br>
 * Name: Proximity I2C Brick<br>
 * </p>
 */
public class I2CProximityProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "proximity";
    }
}
