/*
 ECGProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.ECGProfileConstants;

/**
 * ECG プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての心電図計測機能を提供するAPI.<br/>
 * スマートデバイスに対しての心電図計測機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * ECG Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ECGProfile extends DConnectProfile implements ECGProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }


    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにECGを設定する.
     *
     * @param response レスポンス
     * @param ecg ECGオブジェクト
     */
    public static void setECG(final Intent response, final Bundle ecg) {
        response.putExtra(PARAM_ECG, ecg);
    }

    /**
     * レスポンスに測定値を設定する.
     *
     * @param response レスポンス
     * @param value 測定値
     */
    public static void setValue(final Bundle response, final float value) {
        response.putFloat(PARAM_VALUE, value);
    }

    /**
     * レスポンスにMDER Float値を設定する.
     *
     * @param response レスポンス
     * @param mder MDER Float値
     */
    public static void setMDERFloat(final Bundle response, final String mder) {
        response.putString(PARAM_MDER_FLOAT, mder);
    }
    /**
     * レスポンスにtype値を設定する.
     *
     * @param response レスポンス
     * @param type type
     */
    public static void setType(final Bundle response, final String type) {
        response.putString(PARAM_TYPE, type);
    }
    /**
     * レスポンスにtypeCode値を設定する.
     *
     * @param response レスポンス
     * @param typeCode typeCode
     */
    public static void setTypeCode(final Bundle response, final int typeCode) {
        response.putInt(PARAM_TYPE_CODE, typeCode);
    }
    /**
     * レスポンスにunit値を設定する.
     *
     * @param response レスポンス
     * @param unit unit
     */
    public static void setUnit(final Bundle response, final String unit) {
        response.putString(PARAM_UNIT, unit);
    }
    /**
     * レスポンスにUnitCode値を設定する.
     *
     * @param response レスポンス
     * @param unitCode UnitCode
     */
    public static void setUnitCode(final Bundle response, final int unitCode) {
        response.putInt(PARAM_UNIT_CODE, unitCode);
    }
    /**
     * レスポンスにTimeStamp値を設定する.
     *
     * @param response レスポンス
     * @param timeStamp TimeStamp
     */
    public static void setTimestamp(final Bundle response, final long timeStamp) {
        response.putLong(PARAM_TIMESTAMP, timeStamp);
    }
    /**
     * レスポンスにTimeStampString値を設定する.
     *
     * @param response レスポンス
     * @param timeStampString TimeStampString
     */
    public static void setTimestampString(final Bundle response, final String timeStampString) {
        response.putString(PARAM_TIMESTAMP_STRING, timeStampString);
    }


    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------
}
