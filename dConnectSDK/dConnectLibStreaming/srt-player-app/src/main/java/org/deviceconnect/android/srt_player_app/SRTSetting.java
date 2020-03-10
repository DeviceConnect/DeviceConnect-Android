package org.deviceconnect.android.srt_player_app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

class SRTSetting {

    private SharedPreferences mSharedPreferences;

    SRTSetting(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    void setServerUrl(String url) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("srt_server_url", url);
        editor.apply();
    }

    String getServerUrl() {
        return mSharedPreferences.getString("srt_server_url", null);
    }

    int getRcvLatency() {
        String value = mSharedPreferences.getString("settings_srt_rcvlatency", "120");
        return Integer.parseInt(value);
    }

    int getConnTimeo() {
        String value = mSharedPreferences.getString("settings_srt_conntimeo", "3000");
        return Integer.parseInt(value);
    }

    int getPeerIdleTimeo() {
        String value = mSharedPreferences.getString("settings_srt_peeridletimeo", "5000");
        return Integer.parseInt(value);
    }
}
