/*
 WiFiStateReceiver
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;

/**
 * WiFi Event Receiver.
 *
 * @author NTT DOCOMO, INC.
 */
public class WiFiStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        intent.setClass(context, ThetaDeviceService.class);
        context.startService(intent);
    }

}
