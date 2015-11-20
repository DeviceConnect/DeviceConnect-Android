/*
 WiFiStateReceiver
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * WiFi State Receiver.
 *
 * @author NTT DOCOMO, INC.
 */
public class WifiStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        intent.setClass(context, WifiStateService.class);
        context.startService(intent);
    }

}
