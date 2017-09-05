/*
 BatteryUtils.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * バッテリーの残量を取得するためのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class BatteryUtils {

    private BatteryUtils() {}

    /**
     * バッテリーの残量を取得します.
     * @param context コンテキスト
     * @return バッテリーの残量
     */
    public static float getBatteryLevel(final Context context) {
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentfilter);
        if (batteryStatus == null) {
            return 0.0f;
        } else {
            int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int bsc = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return batteryLevel / (float) bsc;
        }
    }
}
