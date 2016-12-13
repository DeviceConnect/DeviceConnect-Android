/*
 StartupReceiver.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * サービス起動に関わるBroadcastIntentを受け取るReceiver.
 *
 * @author NTT DOCOMO, INC.
 */
public class StartupReceiver extends BroadcastReceiver {

    /**
     * Intent受け取り時.
     *
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // ネットワークに再接続した場合はもう一度サービスを起動する
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null) return;
            boolean isConnected = activeNetwork.isConnectedOrConnecting();
            if (!isConnected) return;
        }
        // その他（端末起動時など）でもサービスを起動する
        Intent serviceIntent = new Intent(context, SimpleBotService.class);
        context.startService(serviceIntent);
    }
}
