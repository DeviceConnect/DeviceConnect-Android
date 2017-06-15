package org.deviceconnect.android.deviceplugin.fabo;

import android.content.Context;
import android.content.SharedPreferences;

class FaBoSettings {

    /**
     * 情報を共有するプリファレンス.
     */
    private SharedPreferences mPreferences;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    FaBoSettings(final Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE);
    }

    /**
     * Local OAuth使用フラグを取得します.
     *
     * @return Local OAuthを使用する場合はtrue、それ以外はfalse
     */
    boolean isUseLocalOAuth() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_security_local_oauth), false);
    }

    /**
     * Local OAuth使用フラグを設定します.
     *
     * @param flag 使用フラグ
     */
    void setUseLocalOAuth(final boolean flag) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_security_local_oauth), flag);
        editor.apply();
    }
}
