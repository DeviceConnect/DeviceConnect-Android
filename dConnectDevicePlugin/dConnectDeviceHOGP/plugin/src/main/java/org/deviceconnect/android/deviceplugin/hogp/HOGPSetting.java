/*
 HOGPSetting.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp;

import android.content.Context;
import android.content.SharedPreferences;

import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;

/**
 * HOGPの設定情報を管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
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
     * キーボードの有効・無効設定を格納するキー.
     */
    private static final String KEY_ENABLED_KEYBOARD = "hid_keyboard";

    /**
     * マウスのモード設定を格納するキー.
     */
    private static final String KEY_MOUSE_MODE = "hid_mouse";

    /**
     * ジョイスティックの有効・無効設定を格納するキー.
     */
    private static final String KEY_ENABLED_JOYSTICK = "hid_joystick";

    /**
     * 設定を保存するプリファレンス.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    HOGPSetting(final Context context) {
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

    /**
     * キーボードの有効・無効設定を行います.
     * @return キーボードの有効・無効
     */
    public boolean isEnabledKeyboard() {
        return mSharedPreferences.getBoolean(KEY_ENABLED_KEYBOARD, false);
    }

    /**
     * キーボードの有効・無効設定を行います.
     * @param flag 有効の場合はtrue、無効の場合はfalse
     */
    public void setEnabledKeyboard(final boolean flag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_ENABLED_KEYBOARD, flag);
        editor.apply();
    }

    /**
     * マウスのモードを取得します.
     * @return マウスモード
     */
    public HOGPServer.MouseMode getMouseMode() {
        return HOGPServer.MouseMode.valueOf(mSharedPreferences.getInt(KEY_MOUSE_MODE, HOGPServer.MouseMode.RELATIVE.getValue()));
    }

    /**
     * マウスのモードを設定します.
     * @param mode マウスモード
     */
    public void setMouseMode(final HOGPServer.MouseMode mode) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_MOUSE_MODE, mode.getValue());
        editor.apply();
    }

    /**
     * ジョイスティックの有効・無効設定を行います.
     * @return ジョイスティックの有効・無効
     */
    public boolean isEnabledJoystick() {
        return mSharedPreferences.getBoolean(KEY_ENABLED_JOYSTICK, false);
    }

    /**
     * ジョイスティックの有効・無効設定を行います.
     * @param flag 有効の場合はtrue、無効の場合はfalse
     */
    public void setEnabledJoystick(final boolean flag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_ENABLED_JOYSTICK, flag);
        editor.apply();
    }
}
