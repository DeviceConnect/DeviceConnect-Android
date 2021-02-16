package org.deviceconnect.android.deviceplugin.uvc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;

import java.util.Set;

/**
 * Properties を使用して、データを保存するためのユーテリティクラス.
 */
public final class PropertyUtil {
    private final SharedPreferences mPref;

    public PropertyUtil(Context context, String name) {
        mPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public Set<String> getKeys() {
        return mPref.getAll().keySet();
    }

    public void put(String key, int value) {
        mPref.edit().putString(key, String.valueOf(value)).apply();
    }

    public void put(String key, long value) {
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

    public void put(String leftKey, String topKey, String rightKey, String bottomKey, Rect rect) {
        mPref.edit().putString(leftKey, String.valueOf(rect.left))
                .putString(topKey, String.valueOf(rect.top))
                .putString(rightKey, String.valueOf(rect.right))
                .putString(bottomKey, String.valueOf(rect.bottom))
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

    public Long getLong(String key, Long defaultValue) {
        String value = mPref.getString(key, String.valueOf(defaultValue));
        try {
            if (value != null) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            // ignore.
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public Float getFloat(String key, Float defaultValue) {
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

    public Rect getRect(String leftKey, String topKey, String rightKey, String bottomKey) {
        String l = mPref.getString(leftKey, null);
        String t = mPref.getString(topKey, null);
        String r = mPref.getString(rightKey, null);
        String b = mPref.getString(bottomKey, null);
        if (l != null && t != null && r != null && b != null) {
            try {
                int left = Integer.parseInt(l);
                int top = Integer.parseInt(t);
                int right = Integer.parseInt(r);
                int bottom = Integer.parseInt(b);
                if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0) {
                    return new Rect(left, top, right, bottom);
                }
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }

    public void remove(String key) {
        mPref.edit().remove(key).apply();
    }
}
