package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static final String PACKAGE_NAME = "org.deviceconnect.android.deviceplugin.switchbot.settings";
    public static final String KEY_LOCAL_OAUTH = "key_local_oauth";

    public static boolean getBoolean(final Context context, final String key, final boolean defaultValue) {
        if (context != null && key != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            if(sharedPreferences != null && sharedPreferences.contains(key)) {
                return sharedPreferences.getBoolean(key, defaultValue);
            }
        }
        return defaultValue;
    }

    static void setBoolean(final Context context, final String key, final boolean value) {
        if (context != null && key != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            if(sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(key, value);
                editor.apply();
            }
        }
    }
}
