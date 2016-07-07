/*
 StressEstimationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.StressEstimationProfileConstants;

/**
 * StressEstimation プロファイル.
 * 
 * <p>
 * スマートデバイスに対してのストレス推定機能を提供するAPI.<br/>
 * スマートデバイスに対してのストレス推定機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Stress Estimation Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class StressEstimationProfile extends DConnectProfile implements StressEstimationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }




    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにStressを設定する.
     *
     * @param response レスポンス
     * @param stress Stressオブジェクト
     */
    public static void setStress(final Intent response, final Bundle stress) {
        response.putExtra(PARAM_STRESS, stress);
    }

    /**
     * レスポンスにLFHFを設定する.
     *
     * @param response レスポンス
     * @param lfhf 測定値
     */
    public static void setLFHF(final Bundle response, final double lfhf) {
        response.putDouble(PARAM_LFHF, lfhf);
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
