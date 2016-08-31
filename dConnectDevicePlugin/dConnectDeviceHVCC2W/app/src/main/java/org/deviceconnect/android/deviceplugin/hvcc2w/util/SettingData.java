/*
 SettingData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * 設定データ管理クラス.
 */
public class SettingData {

    public boolean active = false;
    public String host = null;
    public int port = 0;
    public boolean ssl = false;
    public String clientId = null;
    public String accessToken = null;
    public String serviceId = null;
    public String serviceName = null;
    public Set<String> scopes = null;

    /** Context */
    private Context context;
    /** シングルトン用 */
    private static SettingData instance;

    /**
     * コンストラクタ.
     * シングルトンにするためにprivateとしてある.
     *
     * @param context Context
     */
    private SettingData(Context context) {
        this.context = context;
        load();
    }
    /**
     * 共通のインスタンスを返す.
     *
     * @param context Context
     * @return インスタンス
     */
    public static SettingData getInstance(Context context) {
        if (instance == null) {
            instance = new SettingData(context);
        }
        return instance;
    }

    /**
     * データ保存.
     */
    public void save() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("active", active);
        editor.putString("host", host);
        editor.putInt("port", port);
        editor.putBoolean("ssl", ssl);
        editor.putString("clientId", clientId);
        editor.putString("accessToken", accessToken);
        editor.putString("serviceId", serviceId);
        editor.putString("serviceName", serviceName);
        editor.putStringSet("scopes", scopes);
        editor.apply();
    }

    /**
     * データ読み込み.
     */
    public void load() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        active = preferences.getBoolean("active", false);
        host = preferences.getString("host", "localhost");
        port = preferences.getInt("port", 4035);
        ssl = preferences.getBoolean("ssl", false);
        clientId = preferences.getString("clientId", null);
        accessToken = preferences.getString("accessToken", null);
        serviceId = preferences.getString("serviceId", null);
        serviceName = preferences.getString("serviceName", null);
        scopes = preferences.getStringSet("scopes", null);
    }
}
