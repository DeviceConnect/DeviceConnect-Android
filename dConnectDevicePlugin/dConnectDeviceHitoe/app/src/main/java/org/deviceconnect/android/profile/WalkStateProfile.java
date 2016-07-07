/*
 WalkStateProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.WalkStateProfileConstants;

/**
 * WalkState プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての歩行状態計測機能を提供するAPI.<br/>
 * スマートデバイスに対しての歩行状態計測機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Walk State Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class WalkStateProfile extends DConnectProfile implements WalkStateProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }



    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにWalkを設定する.
     *
     * @param response レスポンス
     * @param walk Walkオブジェクト
     */
    public static void setWalk(final Intent response, final Bundle walk) {
        response.putExtra(PARAM_WALK, walk);
    }

    /**
     * レスポンスに歩数を設定する.
     *
     * @param response レスポンス
     * @param step 歩数
     */
    public static void setStep(final Bundle response, final int step) {
        response.putInt(PARAM_STEP, step);
    }
    /**
     * レスポンスに歩行状態を設定する.
     *
     * @param response レスポンス
     * @param state 状態
     */
    public static void setState(final Bundle response, final String state) {
        response.putString(PARAM_STATE, state);
    }
    /**
     * レスポンスに速度を設定する.
     *
     * @param response レスポンス
     * @param speed 速度
     */
    public static void setSpeed(final Bundle response, final double speed) {
        response.putDouble(PARAM_SPEED, speed);
    }
    /**
     * レスポンスに距離を設定する.
     *
     * @param response レスポンス
     * @param distance 距離
     */
    public static void setDistance(final Bundle response, final double distance) {
        response.putDouble(PARAM_DISTANCE, distance);
    }
    /**
     * レスポンスにバランスを設定する.
     *
     * @param response レスポンス
     * @param balance バランス
     */
    public static void setBalance(final Bundle response, final double balance) {
        response.putDouble(PARAM_BALANCE, balance);
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
