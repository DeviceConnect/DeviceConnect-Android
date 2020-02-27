package org.deviceconnect.android.srt_player_app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SRTSetting {

    private SharedPreferences mSharedPreferences;

    public SRTSetting(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void setServerUrl(String url) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("srt_server_url", url);
        editor.apply();
    }

    public String getServerUrl() {
        return mSharedPreferences.getString("srt_server_url", null);
    }
}
