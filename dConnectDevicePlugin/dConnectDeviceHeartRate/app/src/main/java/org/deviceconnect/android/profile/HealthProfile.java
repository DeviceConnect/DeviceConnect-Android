/*
 HealthProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.profile.HealthProfileConstants;

/**
 * Health プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての健康機器操作機能を提供するAPI.<br/>
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class HealthProfile extends DConnectProfile implements HealthProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに心拍数を設定する.
     * 
     * @param response レスポンス
     * @param heartRate 心拍数
     */
    public static void setHeartRate(final Intent response, final int heartRate) {
        response.putExtra(PARAM_HEART_RATE, heartRate);
    }

}
