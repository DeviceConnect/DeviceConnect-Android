package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

/**
 * I2C用加速度センサープロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #202<br>
 * Name: 9Axis I2C Brick<br>
 * </p>
 */
public class I2C9AxisDeviceOrientationProfile extends BaseFaBoProfile {
    @Override
    public String getProfileName() {
        return "deviceOrientation";
    }
}
