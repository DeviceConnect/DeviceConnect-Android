/*
 DConnectApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.deviceconnect.android.logger.AndroidHandler;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Bluetooth Device Application.
 */
public class DConnectApplication extends Application {

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("deviceconnect");

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("deviceconnect.uiapp");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.INFO);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.INFO);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

    /**
     * アクセストークンを取得する.
     * アクセストークンがない場合にはnullを返却する。
     * @return アクセストークン
     */
    public String getAccessToken() {
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        String accessToken = prefs.getString(
            getString(R.string.key_settings_dconn_access_token), null);
        return accessToken;
    }

    /**
     * クライアントIDを取得する.
     * @return クライアントID
     */
    public String getClientId() {
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        String clientId = prefs.getString(
            getString(R.string.key_settings_dconn_client_id), null);
        return clientId;
    }

    /**
     * SSLフラグを取得する.
     * @return SSLを使用する場合はtrue、それ以外はfalse
     */
    public boolean isSSL() {
        final SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        boolean isSSL = prefs.getBoolean(
            getString(R.string.key_settings_dconn_ssl), false);
        return isSSL;
    }

    /**
     * ホスト名を取得する.
     * @return ホスト名
     */
    public String getHostName() {
        final SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        String host = prefs.getString(
            getString(R.string.key_settings_dconn_host),
            getString(R.string.default_host));
        return host;
    }

    /**
     * ホートを取得する.
     * @return ポート番号
     */
    public int getPort() {
        final SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());

        int port = Integer.parseInt(prefs.getString(
            getString(R.string.key_settings_dconn_port),
            getString(R.string.default_port)));
        return port;
    }
}
