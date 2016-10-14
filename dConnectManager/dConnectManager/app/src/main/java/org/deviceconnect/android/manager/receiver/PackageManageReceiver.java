/*
 PackageManageReceiver.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DevicePluginManager;

/**
 * apkのインストールとアンインストールのブロードキャストを受け取るレシーバー.
 * @author NTT DOCOMO, INC.
 */
public class PackageManageReceiver extends BroadcastReceiver {
    /**
     * DConnectServiceに伝える.
     * @param context コンテキスト
     * @param intent リクエスト
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        DConnectApplication app = (DConnectApplication)context.getApplicationContext();
        DevicePluginManager mgr = app.getDevicePluginManager();

        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            mgr.checkAndAddDevicePlugin(intent);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            mgr.checkAndRemoveDevicePlugin(intent);
        }
    }
}
