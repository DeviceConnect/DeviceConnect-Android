package org.deviceconnect.android.deviceplugin.hogp;

import android.content.Context;
import android.content.SharedPreferences;

public class HOGPSetting {

    /**
     * ファイル名.
     */
    private static final String FILE_NAME = "hogp.dat";

    /**
     * HOGPサーバのOn/Off設定を格納するキー.
     */
    private static final String KEY_ENABLED_SERVER = "server_enabled";

    /**
     * Local OAuthのOn/Off設定を格納するキー.
     */
    private static final String KEY_ENABLED_OAUTH = "local_oauth_enabled";

    /**
     * 設定を保存するプリファレンス.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public HOGPSetting(final Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * HOGPサーバのOn/Off設定を取得します.
     * @return HOGPサーバのOn/Off設定
     */
    public boolean isEnabledServer() {
        return mSharedPreferences.getBoolean(KEY_ENABLED_SERVER, false);
    }

    /**
     * HOGPサーバのOn/Off設定を設定します.
     * @param flag Onの場合はtrue、Offの場合はfalse
     */
    public void setEnabledServer(final boolean flag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_ENABLED_SERVER, flag);
        editor.apply();
    }

    /**
     * Local OAuthのOn/Off設定を取得します.
     * @return Local OAuthのOn/Off設定
     */
    public boolean isEnabledOAuth() {
        return mSharedPreferences.getBoolean(KEY_ENABLED_OAUTH, false);
    }

    /**
     * Local OAuthのOn/Off設定を行います.
     * @param flag Onの場合はtrue、Offの場合はfalse
     */
    public void setEnabledOAuth(final boolean flag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_ENABLED_OAUTH, flag);
        editor.apply();
    }
}
