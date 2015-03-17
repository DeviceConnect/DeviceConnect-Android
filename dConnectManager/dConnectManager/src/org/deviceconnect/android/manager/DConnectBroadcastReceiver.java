/*
 DConnectBroadcastReceiver.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import java.util.List;
import java.util.logging.Logger;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * dConnect Managerへのイベント受信.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DConnectBroadcastReceiver extends BroadcastReceiver {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * 受信したことをDConnectServiceに通知.
     * 
     * @param context コンテキスト
     * @param intent リクエスト
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // DConnectServiceがOFFの場合は、イベント受信によりONにしないようにする.
        if (!isManagerRunning(context)) {
            String action = intent.getAction();
            if (action != null && IntentDConnectMessage.ACTION_EVENT.equals(action)) {
                if (BuildConfig.DEBUG) {
                    String serviceId = DConnectProfile.getServiceID(intent);
                    mLogger.info("Ignored an event message from device plug-in: serviceId=" + serviceId);
                }
                return;
            }
        }

        Intent targetIntent = new Intent(intent);
        targetIntent.setClass(context, DConnectService.class);
        context.startService(targetIntent);
    }

    /**
     * DConnectServiceが起動しているかどうかを確認する.
     * @param context コンテキスト
     * @return 起動中の場合はtrue、それ以外はfalse
     */
    private boolean isManagerRunning(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : runningService) {
            if (DConnectService.class.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
