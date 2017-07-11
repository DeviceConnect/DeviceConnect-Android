package org.deviceconnect.android.deviceplugin.hogp.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public final class BatteryUtils {

    private BatteryUtils() {}

    public static final float getBatteryLevel(final Context context) {
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
