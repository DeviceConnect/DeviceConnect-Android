package org.deviceconnect.android.manager.receiver;

import org.deviceconnect.android.manager.DConnectService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Service to start Device Connect Manager and update HMAC keys for each origin.
 */
public class DConnectManagerLaunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Intent targetIntent = new Intent(intent);
        targetIntent.setClass(context, DConnectService.class);
        context.startService(targetIntent);
    }

}
