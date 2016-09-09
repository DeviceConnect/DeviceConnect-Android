package org.deviceconnect.android.deviceplugin.awsiot.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {

    private SharedPreferences mPreferences;
    private Context mContext;

    public PreferenceUtil(final Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void putValue(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null.");
        }

        SharedPreferences.Editor editor = mPreferences.edit();
        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            throw new IllegalArgumentException();
        }
        editor.apply();
    }

    public String getString(final String key) {
        return mPreferences.getString(key, null);
    }

    public boolean getBoolean(final String key) {
        return mPreferences.getBoolean(key, false);
    }
}
