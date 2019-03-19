/*
 PackageBroadcastReceiver.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectConst;

/**
 * Android OS からアプリケーションのインストール・アンインストール通知を受信するレシーバー.
 *
 * @author NTT DOCOMO, INC.
 */
public class PackageBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action;
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            action = DConnectConst.ACTION_PACKAGE_ADDED;
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            action = DConnectConst.ACTION_PACKAGE_REMOVED;
        } else {
            return;
        }

        Intent message = new Intent(context, DConnectService.class);
        message.setAction(action);
        message.putExtra(DConnectConst.EXTRA_PACKAGE_NAME, getPackageName(intent));
        try {
            context.startService(message);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * インストール・アンインストールしたアプリのパッケージ名を取得する.
     * @param intent パッケージ名を取得するIntent
     * @return パッケージ名
     */
    private String getPackageName(final Intent intent) {
        String pkgName = intent.getDataString();
        int idx = pkgName.indexOf(":");
        if (idx != -1) {
            pkgName = pkgName.substring(idx + 1);
        }
        return pkgName;
    }
}
