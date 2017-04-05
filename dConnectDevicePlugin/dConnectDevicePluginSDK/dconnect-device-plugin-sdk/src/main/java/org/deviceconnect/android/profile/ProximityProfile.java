/*
 ProximityProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.ProximityProfileConstants;

/**
 * Proximity プロファイル.
 * 
 * <p>
 * スマートデバイスの近接センサーの検知通知を提供するAPI.<br>
 * スマートデバイスの近接センサーの検知通知を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class ProximityProfile extends DConnectProfile implements ProximityProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * メッセージに近接距離を設定する.
     * 
     * @param proximity メッセージパラメータ
     * @param value 近接距離
     */
    public static void setValue(final Bundle proximity, final double value) {
        proximity.putDouble(PARAM_VALUE, value);
    }

    /**
     * メッセージに近接距離の最小値を設定する.
     * 
     * @param proximity メッセージパラメータ
     * @param min 最小値
     */
    public static void setMin(final Bundle proximity, final double min) {
        proximity.putDouble(PARAM_MIN, min);
    }

    /**
     * メッセージに近接距離の最大値を設定する.
     * 
     * @param proximity メッセージパラメータ
     * @param max 最大値
     */
    public static void setMax(final Bundle proximity, final double max) {
        proximity.putDouble(PARAM_MAX, max);
    }

    /**
     * メッセージに閾値を設定する.
     * 
     * @param proximity メッセージパラメータ
     * @param threshold 閾値
     */
    public static void setThreshold(final Bundle proximity, final double threshold) {
        proximity.putDouble(PARAM_THRESHOLD, threshold);
    }

    /**
     * メッセージに近接センサー情報を設定する.
     * 
     * @param message メッセージパラメータ
     * @param proximity 近接センサー情報
     */
    public static void setProximity(final Intent message, final Bundle proximity) {
        message.putExtra(PARAM_PROXIMITY, proximity);
    }

    /**
     * メッセージに近接の有無を設定する.
     * 
     * @param message メッセージパラメータ
     * @param near 近接の有無(true: 近接している、false: 近接していない)
     */
    public static void setNear(final Bundle message, final boolean near) {
        message.putBoolean(PARAM_NEAR, near);
    }

    /**
     * メッセージに距離識別子を設定する.
     * @param message メッセージパラメータ
     * @param range 近接識別子
     */
    public static void setRange(final Bundle message, final Range range) {
        message.putString(PARAM_RANGE, range.getValue());
    }
}
