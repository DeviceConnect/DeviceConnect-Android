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
 * PreferenceManagerのユーティリティー.
 */
public final class PreferenceUtil {

    private static final String PREF_LED_ON_COLOR_SETTING = "pref_ledOnColorSetting";
    private static final String PREF_LED_ON_PATTERN_SETTING = "pref_ledOnPatternSetting";
    private static final String PREF_LED_OFF_SETTING = "pref_lightOffSetting";
    private static final String PREF_VIB_ON_SETTING = "pref_vibrationOnSetting";
    private static final String PREF_VIB_OFF_SETTING = "pref_vibrationOffSetting";
    private static final String PREF_BEACON_SCAN_STATUS = "forceBeaconScanStatus";
    private static final String PREF_BEACON_SCAN_MODE = "beaconScanMode";

    private static PreferenceUtil mInstance;

    private SharedPreferences mPreferences;

    private PreferenceUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new PreferenceUtil(context);
        }
        return mInstance;
    }

    public Integer getVibrationOnSetting(final String address) {
        Map<String, Integer> map = getVibrationOnSetting();
        return map == null ? null : map.get(address);
    }

    public void setVibrationOnSetting(final String address, final Integer id) {
        Map<String, Integer> map = getVibrationOnSetting();
        if (map == null) {
            return;
        }
        map.put(address, id);
        putValue(PREF_VIB_ON_SETTING, new JSONObject(map).toString());
    }

    public Integer getVibrationOffSetting(final String address) {
        Map<String, Integer> map = getVibrationOffSetting();
        return map == null ? null : map.get(address);
    }

    public void setVibrationOffSetting(final String address, final Integer id) {
        Map<String, Integer> map = getVibrationOffSetting();
        if (map == null) {
            return;
        }
        map.put(address, id);
        putValue(PREF_VIB_OFF_SETTING, new JSONObject(map).toString());
    }

    public Integer getLEDColorSetting(final String address) {
        Map<String, Integer> map = getLEDColorSetting();
        return map == null ? null : map.get(address);
    }

    public void setLEDColorSetting(final String address, final Integer id) {
        Map<String, Integer> map = getLEDColorSetting();
        if (map == null) {
            return;
        }
        map.put(address, id);
        putValue(PREF_LED_ON_COLOR_SETTING, new JSONObject(map).toString());
    }


    public Integer getLEDPatternSetting(final String address) {
        Map<String, Integer> map = getLEDPatternSetting();
        return map == null ? null : map.get(address);
    }

    public void setLEDPatternSetting(final String address, final Integer id) {
        Map<String, Integer> map = getLEDPatternSetting();
        if (map == null) {
            return;
        }
        map.put(address, id);
        putValue(PREF_LED_ON_PATTERN_SETTING, new JSONObject(map).toString());
    }

    public Integer getLEDOffSetting(final String address) {
        Map<String, Integer> map = getLightOffSetting();
        return map == null ? null : map.get(address);
    }

    public void setLightOffSetting(final String address, final Integer id) {
        Map<String, Integer> map = getLightOffSetting();
        if (map == null) {
            return;
        }
        map.put(address, id);
        putValue(PREF_LED_OFF_SETTING, new JSONObject(map).toString());
    }

    public void setForceBeaconScanStatus(final boolean status) {
        putValue(PREF_BEACON_SCAN_STATUS, status);
    }

    public boolean getForceBeaconScanStatus() {
        return mPreferences.getBoolean(PREF_BEACON_SCAN_STATUS, false);
    }

    public void setBeaconScanMode(final int mode) {
        putValue(PREF_BEACON_SCAN_MODE, mode);
    }

    public int getBeaconScanMode() {
        return mPreferences.getInt(PREF_BEACON_SCAN_MODE, 0);
    }

    private Map<String, Integer> getLEDColorSetting() {
        return getSetting(PREF_LED_ON_COLOR_SETTING);
    }

    private Map<String, Integer> getLEDPatternSetting() {
        return getSetting(PREF_LED_ON_PATTERN_SETTING);
    }

    private Map<String, Integer> getVibrationOnSetting() {
        return getSetting(PREF_VIB_ON_SETTING);
    }

    private Map<String, Integer> getVibrationOffSetting() {
        return getSetting(PREF_VIB_OFF_SETTING);
    }

    private Map<String, Integer> getLightOffSetting() {
        return getSetting(PREF_LED_OFF_SETTING);
    }

    private Map<String, Integer> getSetting(final String pref) {
        String jsonString = mPreferences.getString(pref, null);
        if (jsonString == null) {
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

    private void putValue(final String key, final Object value) {
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
}
