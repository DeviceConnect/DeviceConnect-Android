package com.example.switchbotdemoapp.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static final String SETTINGS_NAME = "com.example.switchbotdemoapp.utility.settings";

    public static String getString(final Context context, final String key, final String defaultValue) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    SETTINGS_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                return sharedPreferences.getString(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static void putString(final Context context, final String key, final String value) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    SETTINGS_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (editor != null) {
                    editor.putString(key, value);
                    editor.apply();
                }
            }
        }
    }

    public static void remove(final Context context, final String key) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    SETTINGS_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (editor != null) {
                    editor.remove(key);
                    editor.apply();
                }
            }
        }
    }
}
