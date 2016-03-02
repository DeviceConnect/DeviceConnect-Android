/*
 PreferenceUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * PreferenceManagerのユーティリティー
 */
final public class PreferenceUtil {

    private static PreferenceUtil mInstance;

    private SharedPreferences mPreferences;
    private Context mContext;

    private PreferenceUtil(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferenceUtil(context);
        }
        return mInstance;
    }

    public void setVibrationOffSetting(Map<String, Integer> map) {
        JSONObject obj = new JSONObject(map);
        putValue("pref_vibrationOffSetting", obj.toString());
    }

    public Map<String, Integer> getVibrationOffSetting() {
        String jsonString = mPreferences.getString("pref_vibrationOffSetting", "");
        if (jsonString.equals("")) {
            return new HashMap<>();
        }
        try {
            JSONObject obj = new JSONObject(jsonString);
            Map<String, Integer> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : JsonUtil.toMap(obj).entrySet()) {
                try {
                    newMap.put(entry.getKey(), (Integer) entry.getValue());
                } catch (ClassCastException cce) {
                    return null;
                }
            }
            return newMap;
        } catch (JSONException e) {
            return null;
        }
    }

    public void setLightOffSetting(Map<String, Integer> map) {
        JSONObject obj = new JSONObject(map);
        putValue("pref_lightOffSetting", obj.toString());
    }

    public Map<String, Integer> getLightOffSetting() {
        String jsonString = mPreferences.getString("pref_lightOffSetting", "");
        if (jsonString.equals("")) {
            return new HashMap<>();
        }
        try {
            JSONObject obj = new JSONObject(jsonString);
            Map<String, Integer> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : JsonUtil.toMap(obj).entrySet()) {
                try {
                    newMap.put(entry.getKey(), (Integer) entry.getValue());
                } catch (ClassCastException cce) {
                    return null;
                }
            }
            return newMap;
        } catch (JSONException e) {
            return null;
        }
    }

    public void putValue(String key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Editor editor = mPreferences.edit();
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

    private String getString(int id) {
        return mContext.getString(id);
    }

    private boolean getBoolean(int id) {
        return Boolean.parseBoolean(mContext.getString(id));
    }

    private int getInt(int id) {
        return Integer.parseInt(mContext.getString(id));
    }

    private long getLong(int id) {
        return Long.parseLong(mContext.getString(id));
    }

}
