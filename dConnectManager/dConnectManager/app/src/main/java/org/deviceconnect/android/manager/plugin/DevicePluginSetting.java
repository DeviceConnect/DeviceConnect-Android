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
}
