package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.fabo.R;

public final class Util {

    private Util() {}

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
            case I2C_TEMPERATURE:
                return context.getString(R.string.activity_fabo_virtual_i2c_temperature);
        }
        return null;
    }
}
