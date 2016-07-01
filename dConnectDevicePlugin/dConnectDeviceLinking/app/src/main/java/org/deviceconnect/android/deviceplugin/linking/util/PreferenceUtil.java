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
public final class PreferenceUtil {

    private static PreferenceUtil mInstance;

    private SharedPreferences mPreferences;
    private Context mContext;

    private PreferenceUtil(final Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static PreferenceUtil getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new PreferenceUtil(context);
        }
        return mInstance;
    }

    public void setVibrationOffSetting(final Map<String, Integer> map) {
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

    public void setLightOffSetting(final Map<String, Integer> map) {
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

    public void setForceBeaconScanStatus(final boolean status) {
        putValue("forceBeaconScanStatus", status);
    }

    public boolean getForceBeaconScanStatus() {
        return mPreferences.getBoolean("forceBeaconScanStatus", false);
    }

    public void setBeaconScanStatus(final boolean status) {
        putValue("beaconScanStatus", status);
    }

    public boolean getBeaconScanStatus() {
        return mPreferences.getBoolean("beaconScanStatus", false);
    }

    public void setBeaconScanMode(final int mode) {
        putValue("beaconScanMode", mode);
    }

    public int getBeaconScanMode() {
        return mPreferences.getInt("beaconScanMode", 0);
    }

    public void putValue(final String key, final Object value) {
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

    private String getString(final int id) {
        return mContext.getString(id);
    }

    private boolean getBoolean(final int id) {
        return Boolean.parseBoolean(mContext.getString(id));
    }

    private int getInt(final int id) {
        return Integer.parseInt(mContext.getString(id));
    }

    private long getLong(final int id) {
        return Long.parseLong(mContext.getString(id));
    }

}
