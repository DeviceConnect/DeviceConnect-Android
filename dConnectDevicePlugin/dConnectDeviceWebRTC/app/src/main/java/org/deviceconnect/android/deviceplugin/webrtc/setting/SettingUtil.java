/*
 SettingUtil.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting;

import android.content.Context;
import android.content.SharedPreferences;

import org.deviceconnect.android.deviceplugin.webrtc.R;

/**
 * 設定のユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class SettingUtil {

    private static final String SUFFIX = "_preferences";

    private static SharedPreferences create(final Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName() + SUFFIX,
                Context.MODE_MULTI_PROCESS);
        return sp;
    }

    /**
     * 設定された端末名を取得する.
     *
     * @param context コンテキストオブジェクト
     */
    public static String getDeviceName(final Context context) {
        SharedPreferences sp = create(context);
        return sp.getString("deviceName", context.getString(R.string.settings_default_name));
    }

    /**
     * 端末名を設定する.
     *
     * @param context コンテキストオブジェクト
     * @param deviceName 端末名
     */
    public static void setDeviceName(final Context context, final String deviceName) {
        SharedPreferences sp = create(context);
        sp.edit().putString("deviceName", deviceName).apply();
    }

    /**
     * カメラ設定を取得する.
     *
     * @param context コンテキストオブジェクト
     */
    public static String getCameraParam(final Context context) {
        SharedPreferences sp = create(context);
        return sp.getString("camera", "");
    }

    /**
     * カメラ設定を保存する.
     *
     * @param context コンテキストオブジェクト
     * @param text 設定
     */
    public static void setCameraParam(final Context context, final String text) {
        SharedPreferences sp = create(context);
        sp.edit().putString("camera", text).apply();
    }

    /**
     * 音声設定を取得する.
     *
     * @param context コンテキストオブジェクト
     */
    public static String getAudioParam(final Context context) {
        SharedPreferences sp = create(context);
        return sp.getString("audio", "");
    }

    /**
     * 音声設定を保存する.
     *
     * @param context コンテキストオブジェクト
     * @param text 設定
     */
    public static void setAudioParam(final Context context, final String text) {
        SharedPreferences sp = create(context);
        sp.edit().putString("audio", text).apply();
    }

}
