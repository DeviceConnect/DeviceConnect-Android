/*
 DConnectMessageReceiver.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Device Connect ManagerからのIntentを待ち受けるBroadcastReceiverクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        IntentDConnectSDK.onReceivedResponse(intent);
    }
}
