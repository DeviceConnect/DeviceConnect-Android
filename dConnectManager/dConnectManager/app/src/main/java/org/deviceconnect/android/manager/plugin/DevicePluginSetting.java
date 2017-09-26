/*
 DevicePluginSetting.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * デバイスプラグイン設定クラス.
 *
 * <p>
 * 特定のデバイスプラグインについての設定を永続化する.
 * 具体的には、1つのデバイスプラグインごとに1つのSharedPreferencesオブジェクトを作成する。
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
class DevicePluginSetting {

    /** 設定ファイル名のプレフィクス. */
    private static final String PREFIX_PREFERENCES = "plugin_preferences_";
    /** 設定キー: 有効状態. */
    private static final String KEY_ENABLED = "enabled";

    /**
     * 設定キー: 平均通信時間.
     */
    private static final String KEY_AVERAGE_BAUD_RATE = "average_baud_rate";

    /**
     * 設定キー: 最遅通信時間.
     */
    private static final String KEY_WORST_BAUD_RATE = "worst_baud_rate";

    /**
     * 設定キー: 最遅通信時間のリクエスト.
     */
    private static final String KEY_WORST_REQUEST = "worst_request";

    /** デバイスプラグイン設定を永続化するオブジェクト. */
    private final SharedPreferences mPreferences;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param pluginId プラグインID
     */
    DevicePluginSetting(final Context context, final String pluginId) {
        String prefName = PREFIX_PREFERENCES + pluginId;
        mPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    /**
     * プラグインがユーザーによって有効にされているかどうかを取得する.
     * @return 有効にされている場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    boolean isEnabled() {
        return mPreferences.getBoolean(KEY_ENABLED, true);
    }

    /**
     * プラグインがユーザーによって有効にされているかどうかを設定する.
     * @param isEnabled 有効にされている場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    void setEnabled(final boolean isEnabled) {
        mPreferences.edit().putBoolean(KEY_ENABLED, isEnabled).apply();
    }

    /**
     * すべての設定をクリアし、初期状態に戻す.
     */
    void clear() {
        mPreferences.edit().clear().apply();
    }

    /**
     * 平均通信時間を保存します.
     * @param baudRate 保存する平均通信時間
     */
    void setAverageBaudRate(final long baudRate) {
        mPreferences.edit().putLong(KEY_AVERAGE_BAUD_RATE, baudRate).apply();
    }

    /**
     * 平均通信時間を取得します.
     * @return 平均通信時間
     */
    long getAverageBaudRate() {
        return mPreferences.getLong(KEY_AVERAGE_BAUD_RATE, 0);
    }

    /**
     * 最遅通信時間を保存します.
     * @param baudRate 最遅通信時間
     */
    void setWorstBaudRate(final long baudRate) {
        mPreferences.edit().putLong(KEY_WORST_BAUD_RATE, baudRate).apply();
    }

    /**
     * 最遅通信時間を取得します.
     * @return 最遅通信時間
     */
    long getWorstBaudRate() {
        return mPreferences.getLong(KEY_WORST_BAUD_RATE, 0);
    }

    /**
     * 最遅通信時間のリクエストを保存します.
     * @param request 最遅通信時間のリクエスト
     */
    void setWorstRequest(final String request) {
        mPreferences.edit().putString(KEY_WORST_REQUEST, request).apply();
    }

    /**
     * 最遅通信時間のリクエストを取得します.
     * @return 最遅通信時間のリクエスト
     */
    String getWorstRequest() {
        return mPreferences.getString(KEY_WORST_REQUEST, "None");
    }
}
