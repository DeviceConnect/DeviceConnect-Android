/*
 HvcDeviceReceiver.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Notify Broadcast Receiver.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        intent.setClass(context, HvcDeviceService.class);
        context.startService(intent);
    }
}
