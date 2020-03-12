package org.deviceconnect.android.rtspplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class RTSPSetting {

    private SharedPreferences mSharedPreferences;

    RTSPSetting(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    void setServerUrl(String url) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("rtsp_server_url", url);
        editor.apply();
    }

    String getServerUrl() {
        return mSharedPreferences.getString("rtsp_server_url", null);
    }

    boolean isEnabledDebugLog() {
        return mSharedPreferences.getBoolean("settings_debug_log", true);
    }
}
