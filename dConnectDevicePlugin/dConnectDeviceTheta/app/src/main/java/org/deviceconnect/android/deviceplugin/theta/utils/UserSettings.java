/*
UserSettings
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.theta.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * UserSettings.
 * @author NTT DOCOMO, INC.
 */
public class UserSettings {
    /** プリファレンス名. */
    private static final String PREF_NAME = "theta_shared_pref";
    /** SSIDのキー名. */
    private static final String KEY_SSID = "ssid";

    /** SharedPreferences' instance. */
    private SharedPreferences mPref;
    /** SharedPreferences.Editor's instance. */
    private SharedPreferences.Editor mEditor;

    /**
     * Constructor.
     * @param context Context in which this class belongs
     */
    public UserSettings(final Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * Get the SSID registered.
     * Returns null if the SSID is not registered.
     * @return SSID
     */
    public String getSSID() {
        return mPref.getString(KEY_SSID, null);
    }

    /**
     * Register the SSID.
     * Old SSID to override.
     * @param ssid Register the SSID
     */
    public void setSSID(final String ssid) {
        mEditor = mPref.edit();
        mEditor.putString(KEY_SSID, ssid);
        mEditor.commit();
    }
    /**
     * Set a password for the SSID.
     *@param ssid SSID
     *@param password password
     */
    public void setSSIDPassword(final String ssid, final String password) {
        mEditor = mPref.edit();
        mEditor.putString(ssid, password);
        mEditor.commit();
    }
    /**
     * Get the password for the SSID.
     *@param ssid SSID
     *@return password
     */
    public String getSSIDPassword(final String ssid) {
        return mPref.getString(ssid, null);
    }
}
