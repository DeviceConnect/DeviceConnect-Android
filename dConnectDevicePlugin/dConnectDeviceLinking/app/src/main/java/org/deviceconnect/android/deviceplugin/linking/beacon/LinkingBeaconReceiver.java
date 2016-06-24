/*
 LinkingBeaconReceiver.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;

public class LinkingBeaconReceiver extends BroadcastReceiver {
    @Override
    public final void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action) ||
                LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
            intent.setClass(context, LinkingDeviceService.class);
            context.startService(intent);
        }
    }
}
