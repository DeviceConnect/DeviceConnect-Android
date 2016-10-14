/*
 PoseEstimationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.PoseEstimationProfileConstants;

/**
 * PoseEstimation プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての姿勢推定機能を提供するAPI.<br/>
 * スマートデバイスに対しての姿勢推定機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Pose Estimation Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class PoseEstimationProfile extends DConnectProfile implements PoseEstimationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }



    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにPoseを設定する.
     *
     * @param response レスポンス
     * @param stress Poseオブジェクト
     */
    public static void setPose(final Intent response, final Bundle stress) {
        response.putExtra(PARAM_STRESS, stress);
    }

    /**
     * レスポンスに姿勢状態を設定する.
     *
     * @param response レスポンス
     * @param state 姿勢
     */
    public static void setState(final Bundle response, final String state) {
        response.putString(PARAM_STATE, state);
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
