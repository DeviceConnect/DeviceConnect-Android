package org.deviceconnect.message.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TestBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        intent.setClass(context, TestService.class);
        context.startService(intent);
    }
}
