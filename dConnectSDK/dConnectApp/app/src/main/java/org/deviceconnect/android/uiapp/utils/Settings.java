package org.deviceconnect.android.uiapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.deviceconnect.android.uiapp.R;

public final class Settings {

    private SharedPreferences mPreferences;
    private Context mContext;

    private static Settings mInstance;

    public static Settings getInstance() {
        if (mInstance == null) {
            mInstance = new Settings();
        }
        return mInstance;
    }

    public void load(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE);
    }

    /**
     * アクセストークンを取得する.
     * アクセストークンがない場合にはnullを返却する。
     * @return アクセストークン
     */
    public String getAccessToken() {
        String accessToken = mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_access_token), null);
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_access_token), accessToken);
        editor.commit();
    }

    /**
     * クライアントIDを取得する.
     * @return クライアントID
     */
    public String getClientId() {
        String clientId = mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_client_id), null);
        return clientId;
    }

    public void setClientId(final String clientId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_client_id), clientId);
        editor.commit();
    }

    /**
     * SSLフラグを取得する.
     * @return SSLを使用する場合はtrue、それ以外はfalse
     */
    public boolean isSSL() {
        boolean isSSL = mPreferences.getBoolean(
                mContext.getString(R.string.key_settings_dconn_ssl), false);
        return isSSL;
    }

    /**
     * ホスト名を取得する.
     * @return ホスト名
     */
    public String getHostName() {
        String host = mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_host),
                mContext.getString(R.string.default_host));
        return host;
    }

    /**
     * ホートを取得する.
     * @return ポート番号
     */
    public int getPort() {
        int port = Integer.parseInt(mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_port),
                mContext.getString(R.string.default_port)));
        return port;
    }

    public String getSDKType() {
        String type = mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_sdk),
                mContext.getString(R.string.activity_settings_sdk_entry1));
        return type;
    }
}
