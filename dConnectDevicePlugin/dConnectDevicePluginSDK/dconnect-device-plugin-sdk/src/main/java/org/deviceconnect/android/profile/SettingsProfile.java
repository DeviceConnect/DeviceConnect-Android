/*
 SettingsProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants.VolumeKind;

import android.content.Intent;

/**
 * Settings プロファイル.
 * 
 * <p>
 * スマートデバイスの各種設定状態の取得および設定機能を提供するAPI.<br>
 * スマートデバイスの各種設定状態の取得および設定機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Settings Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>Volume Settings API [GET] :
 * {@link SettingsProfile#onGetSoundVolume(Intent, Intent, String, 
 * org.deviceconnect.profile.SettingsProfileConstants.VolumeKind)}</li>
 * <li>Volume Settings API [PUT] :
 * {@link SettingsProfile#onPutSoundVolume(Intent, Intent, String, VolumeKind, Double)}</li>
 * <li>Date Settings API [GET] :
 * {@link SettingsProfile#onGetDate(Intent, Intent, String)}</li>
 * <li>Date Settings API [PUT] :
 * {@link SettingsProfile#onPutDate(Intent, Intent, String, String)}</li>
 * <li>Display Light Settings API [GET] :
 * {@link SettingsProfile#onGetDisplayLight(Intent, Intent, String)}</li>
 * <li>Display Light Settings API [PUT] :
 * {@link SettingsProfile#onPutDisplayLight(Intent, Intent, String, Double)}</li>
 * <li>Display Sleep Settings API [GET] :
 * {@link SettingsProfile#onGetDisplaySleep(Intent, Intent, String)}</li>
 * <li>Display Sleep Settings API [PUT] :
 * {@link SettingsProfile#onPutDisplaySleep(Intent, Intent, String, Integer)}</li>
 * </ul>
 * @author NTT DOCOMO, INC.
 */
public abstract class SettingsProfile extends DConnectProfile implements SettingsProfileConstants {

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
