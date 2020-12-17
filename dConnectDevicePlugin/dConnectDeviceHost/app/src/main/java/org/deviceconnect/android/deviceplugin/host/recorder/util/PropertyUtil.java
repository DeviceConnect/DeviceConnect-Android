package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Size;

import java.util.Set;

/**
 * Properties を使用して、データを保存するためのユーテリティクラス.
 */
public final class PropertyUtil {
    private SharedPreferences mPref;

    public PropertyUtil(Context context, String name) {
        mPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().commit();
    }

    public Set<String> getKeys() {
        return mPref.getAll().keySet();
    }

    public void put(String key, int value) {
        mPref.edit().putString(key, String.valueOf(value)).apply();
    }

    public void put(String key, float value) {
        mPref.edit().putString(key, String.valueOf(value)).apply();
    }

    public void put(String key, boolean value) {
        mPref.edit().putBoolean(key, value).apply();
    }

    public void put(String key, String value) {
        mPref.edit().putString(key, value).apply();
    }

    public void put(String widthKey, String heightKey, Size size) {
        mPref.edit().putString(widthKey, String.valueOf(size.getWidth()))
                .putString(heightKey, String.valueOf(size.getHeight()))
                .apply();
    }

    public Integer getInteger(String key, Integer defaultValue) {
        String value = mPref.getString(key, String.valueOf(defaultValue));
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            // ignore.
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        String value = mPref.getString(key, String.valueOf(defaultValue));
        try {
            if (value != null) {
                return Float.parseFloat(value);
            }
        } catch (Exception e) {
            // ignore.
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    public Size getSize(String widthKey, String heightKey) {
        String w = mPref.getString(widthKey, null);
        String h = mPref.getString(heightKey, null);
        if (w != null && h != null) {
            try {
                return new Size(Integer.parseInt(w), Integer.parseInt(h));
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }
}
