/*
UserSettings
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hitoe.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * UserSettings.
 * @author NTT DOCOMO, INC.
 */
public class UserSettings {
    /** Preference name. */
    private static final String PREF_NAME = "hitoe_setting_pref";
    /** Key of next on. */
    private static final String KEY_HITOE_ON_NEXT = "next_on";
    /** Key of warning message. */
    private static final String KEY_WARNING_MESSAGE = "warning_message";

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
     * Get the Next state registered.
     * Returns null if the Next state is not registered.
     * @return true:next show false:next not show
     */
    public boolean isNextState() {
        return mPref.getBoolean(KEY_HITOE_ON_NEXT, false);
    }

    /**
     * Get the Warning message flag.
     * @return true:next show false:next not show
     */
    public boolean isWarningMessage() {
        return mPref.getBoolean(KEY_WARNING_MESSAGE, false);
    }

    /**
     * Register the Next state.
     * @param state true:next show false:next not show
     */
    public void setNextState(final boolean state) {
        mEditor = mPref.edit();
        mEditor.putBoolean(KEY_HITOE_ON_NEXT, state);
        mEditor.commit();
    }

    /**
     * Register the Warning Message flag.
     * @param state true:next show false:next not show
     */
    public void setWarningMessage(final boolean state) {
        mEditor = mPref.edit();
        mEditor.putBoolean(KEY_WARNING_MESSAGE, state);
        mEditor.commit();
    }
}
