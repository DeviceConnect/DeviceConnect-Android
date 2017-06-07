package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用加速度センサープロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #201<br>
 * Name: 3Axis I2C Brick<br>
 * </p>
 */
public class I2C3AxisDeviceOrientationProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "deviceOrientation";
    }
}
