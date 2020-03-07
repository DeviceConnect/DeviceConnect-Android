package org.deviceconnect.android.rtspplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RTSPSetting {

    private SharedPreferences mSharedPreferences;

    public RTSPSetting(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void setServerUrl(String url) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("rtsp_server_url", url);
        editor.apply();
    }

    public String getServerUrl() {
        return mSharedPreferences.getString("rtsp_server_url", null);
    }
}
