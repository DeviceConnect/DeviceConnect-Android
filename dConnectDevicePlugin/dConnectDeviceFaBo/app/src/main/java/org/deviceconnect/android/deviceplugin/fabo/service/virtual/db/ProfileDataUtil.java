package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.fabo.core.R;

import java.util.HashMap;
import java.util.Map;

import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_HUMIDITY;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_ILLUMINANCE;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_KEY_EVENT;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_LIGHT;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_PROXIMITY;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_TEMPERATURE;
import static org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData.Type.GPIO_VIBRATION;

public final class ProfileDataUtil {

    private ProfileDataUtil() {}

    /**
     * プロファイルが使用できるピンのタイプを保持するマップ.
     */
    private static Map<ProfileData.Type, PinType> mPinTypeMap = new HashMap<>();

    /**
     * プロファイルが複数のピンの操作ができるのかを保持するマップ.
     */
    private static Map<ProfileData.Type, Boolean> mMultiChoiceMap = new HashMap<>();

    static {
        mPinTypeMap.put(GPIO_LIGHT, PinType.DIGITAL);
        mPinTypeMap.put(GPIO_TEMPERATURE, PinType.ANALOG);
        mPinTypeMap.put(GPIO_VIBRATION, PinType.DIGITAL);
        mPinTypeMap.put(GPIO_ILLUMINANCE, PinType.ANALOG);
        mPinTypeMap.put(GPIO_KEY_EVENT, PinType.ALL);
        mPinTypeMap.put(GPIO_HUMIDITY, PinType.ANALOG);
        mPinTypeMap.put(GPIO_PROXIMITY, PinType.ANALOG);

        mMultiChoiceMap.put(GPIO_LIGHT, true);
        mMultiChoiceMap.put(GPIO_TEMPERATURE, false);
        mMultiChoiceMap.put(GPIO_VIBRATION, true);
        mMultiChoiceMap.put(GPIO_ILLUMINANCE, false);
        mMultiChoiceMap.put(GPIO_KEY_EVENT, true);
        mMultiChoiceMap.put(GPIO_HUMIDITY, false);
        mMultiChoiceMap.put(GPIO_PROXIMITY, false);
    }

    /**
     * プロファイルの名前を取得します.
     * @param context コンテキスト
     * @param type プロファイルのタイプ
     * @return プロファイル名
     */
    public static String getProfileName(final Context context, final ProfileData.Type type) {
        switch (type) {
            case GPIO_LIGHT:
                return context.getString(R.string.activity_fabo_virtual_gpio_light);
            case GPIO_TEMPERATURE:
                return context.getString(R.string.activity_fabo_virtual_gpio_temperature);
            case GPIO_VIBRATION:
                return context.getString(R.string.activity_fabo_virtual_gpio_vibration);
            case GPIO_ILLUMINANCE:
                return context.getString(R.string.activity_fabo_virtual_gpio_illuminance);
            case GPIO_KEY_EVENT:
                return context.getString(R.string.activity_fabo_virtual_gpio_key_event);
            case GPIO_HUMIDITY:
                return context.getString(R.string.activity_fabo_virtual_gpio_humidity);
            case GPIO_PROXIMITY:
                return context.getString(R.string.activity_fabo_virtual_gpio_proximity);
            case I2C_ROBOT_DRIVE_CONTROLLER:
                return context.getString(R.string.activity_fabo_virtual_i2c_robot_drivecontroller);
            case I2C_MOUSE_DRIVE_CONTROLLER:
                return context.getString(R.string.activity_fabo_virtual_i2c_mouse_drivecontroller);
            case I2C_3AXIS_DEVICE_ORIENTATION:
                return context.getString(R.string.activity_fabo_virtual_i2c_3axis_device_orientation);
            case I2C_TEMPERATURE:
                return context.getString(R.string.activity_fabo_virtual_i2c_temperature);
            case I2C_HUMIDITY:
                return context.getString(R.string.activity_fabo_virtual_i2c_humidity);
            case I2C_PROXIMITY:
                return context.getString(R.string.activity_fabo_virtual_i2c_proximity);
            case I2C_ILLUMINANCE:
                return context.getString(R.string.activity_fabo_virtual_i2c_illuminace);
            case I2C_ATMOSPHERIC_PRESSURE:
                return context.getString(R.string.activity_fabo_virtual_i2c_atmospheric_pressure);
            case I2C_LIDARLITE_PROXIMITY:
                return context.getString(R.string.activity_fabo_virtual_i2c_lidarlite_proximity);
        }
        return null;
    }

    /**
     * プロファイルが使用できるピンのタイプを取得します.
     * @param profileData プロファイルデータ
     * @return 使用できるピンのタイプ
     */
    public static PinType getPinType(final ProfileData profileData) {
        return mPinTypeMap.get(profileData.getType());
    }


    /**
     * プロファイルが使用できるピンのタイプを取得します.
     * @param type プロファイルタイプ
     * @return 使用できるピンのタイプ
     */
    public static PinType getPinType(final ProfileData.Type type) {
        return mPinTypeMap.get(type);
    }

    /**
     * プロファイルが複数のピンを使用できるかを確認します.
     * @param profileData プロファイルデータ
     * @return 複数選択できる場合はtrue、それ以外はfalse
     */
    public static boolean isMultiChoicePin(final ProfileData profileData) {
        return mMultiChoiceMap.get(profileData.getType());
    }

    /**
     * プロファイルが複数のピンを使用できるかを確認します.
     * @param type プロファイルデータ
     * @return 複数選択できる場合はtrue、それ以外はfalse
     */
    public static boolean isMultiChoicePin(final ProfileData.Type type) {
        return mMultiChoiceMap.get(type);
    }


    /**
     * プロファイルが使用できるピンのタイプ.
     */
    public enum PinType {
        /**
         * AnalogとDigitalの療法が使用できます.
         */
        ALL,

        /**
         * Analogが使用できます.
         */
        ANALOG,

        /**
         * Digitalが使用できます.
         */
        DIGITAL
    }
}
