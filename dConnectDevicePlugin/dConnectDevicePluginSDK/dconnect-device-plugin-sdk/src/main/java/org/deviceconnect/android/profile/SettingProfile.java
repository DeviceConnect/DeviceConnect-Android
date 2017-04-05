/*
 SettingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.profile.SettingProfileConstants;

/**
 * Settings プロファイル.
 * 
 * <p>
 * スマートデバイスの各種設定状態の取得および設定機能を提供するAPI.<br>
 * スマートデバイスの各種設定状態の取得および設定機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class SettingProfile extends DConnectProfile implements SettingProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストから音量種別を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 通知タイプ。無い場合は{@link VolumeKind#UNKNOWN}を返す。
     *         <ul>
     *         <li>{@link VolumeKind#ALARM}</li>
     *         <li>{@link VolumeKind#CALL}</li>
     *         <li>{@link VolumeKind#RINGTONE}</li>
     *         <li>{@link VolumeKind#MAIL}</li>
     *         <li>{@link VolumeKind#OTHER}</li>
     *         <li>{@link VolumeKind#UNKNOWN}</li>
     *         </ul>
     */
    public static VolumeKind getVolumeKind(final Intent request) {
        Integer value = parseInteger(request, PARAM_KIND);
        if (value == null) {
            value = VolumeKind.UNKNOWN.getValue();
        }
        VolumeKind kind = VolumeKind.getInstance(value);
        return kind;
    }

    /**
     * リクエストから音量を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 音量。無い場合はnullを返す。
     */
    public static Double getVolumeLevel(final Intent request) {
        return getLevel(request);
    }

    /**
     * リクエストからバックライト明度を取得する.
     * 
     * @param request リクエストパラメータ
     * @return バックライト明度。無い場合はnullを返す。
     */
    public static Double getLightLevel(final Intent request) {
        return getLevel(request);
    }

    /**
     * リクエストから音量を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 音量。無い場合はnullを返す。
     */
    private static Double getLevel(final Intent request) {
        return parseDouble(request, PARAM_LEVEL);
    }

    /**
     * リクエストから日時を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 日時文字列。 無い場合はnullを返す。
     */
    public static String getDate(final Intent request) {
        String date = request.getStringExtra(PARAM_DATE);
        return date;
    }

    /**
     * リクエストから消灯するまでの時間(ミリ秒)を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 消灯するまでの時間(ミリ秒)。無い場合はnullを返す。
     */
    public static Integer getTime(final Intent request) {
        return parseInteger(request, PARAM_TIME);
    }

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにlevelを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param level パーセント
     */
    private static void setLevel(final Intent response, final double level) {

        if (level < MIN_LEVEL || MAX_LEVEL < level) {
            throw new IllegalArgumentException("Level must be between " + MIN_LEVEL + " and " + MAX_LEVEL + ".");
        }

        response.putExtra(PARAM_LEVEL, level);
    }

    /**
     * レスポンスに音量を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param level 音量
     */
    public static void setVolumeLevel(final Intent response, final double level) {
        setLevel(response, level);
    }

    /**
     * レスポンスにバックライト明度を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param level 音量
     */
    public static void setLightLevel(final Intent response, final double level) {
        setLevel(response, level);
    }

    /**
     * レスポンスに日時を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param date 日時。フォーマットはYYYY-MM-DDThh:mm:ss+0900(RFC3339)。
     */
    public static void setDate(final Intent response, final String date) {
        // TODO フォーマットチェックをすべきか？
        response.putExtra(PARAM_DATE, date);
    }

    /**
     * レスポンスに消灯するまでの時間(ミリ秒)を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param time 消灯するまでの時間(ミリ秒)
     */
    public static void setTime(final Intent response, final int time) {

        if (time < 0) {
            throw new IllegalArgumentException("Time must be more than 0.");
        }

        response.putExtra(PARAM_TIME, time);
    }
}
