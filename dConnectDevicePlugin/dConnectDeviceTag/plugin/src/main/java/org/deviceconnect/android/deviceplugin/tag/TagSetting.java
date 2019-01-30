/*
 TagSetting.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * プラグインの設定を保持するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class TagSetting {
    /**
     * 保存するファイル名.
     */
    public static final String FILE_NAME = "tag-setting.dat";

    /**
     * 設定を保存するクラス.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 設定変更通知用リスナー.
     */
    private OnChangeListener mOnChangeListener;

    /**
     * SharedPreferenceの値が変更されたことを受信するリスナー.
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = (sharedPreferences, s) -> {
        if (s != null && s.equalsIgnoreCase(mContext.getString(R.string.key_settings_ouath_on_off))) {
            if (mOnChangeListener != null) {
                mOnChangeListener.onChangedOAuth(mSharedPreferences.getBoolean(s, false));
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public TagSetting(final Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 設定が変更されたことを通知するリスナーを登録します.
     *
     * @param listener リスナー
     */
    public void registerOnChangeListener(final OnChangeListener listener) {
        mOnChangeListener = listener;
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mChangeListener);
    }

    /**
     * 設定が変更されたことを通知するリスナーを解除します.
     */
    public void unregisterOnChangeListener() {
        mOnChangeListener = null;
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mChangeListener);
    }

    /**
     * Local OAuth が有効になっているか確認します.
     *
     * @return LocalOAuthが有効の場合はtrue、それ以外はfalse
     */
    public boolean isUseOAuth() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_settings_ouath_on_off), false);
    }

    /**
     * 設定が変更されたことを通知するリスナー.
     */
    public interface OnChangeListener {
        /**
         * Local OAuth の設定が変更されたことを通知します.
         *
         * @param flag 有効にされた場合はtrue、それ以外はfalse
         */
        void onChangedOAuth(boolean flag);
    }
}
