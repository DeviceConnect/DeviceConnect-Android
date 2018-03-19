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

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;

/**
 * apkのインストールとアンインストールのブロードキャストを受け取るレシーバー.
 * @author NTT DOCOMO, INC.
 */
public class PackageManageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action;
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            action = DConnectMessageService.ACTION_PACKAGE_ADDED;
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            action = DConnectMessageService.ACTION_PACKAGE_REMOVED;
        } else {
            return;
        }

        Intent message = new Intent(context, DConnectService.class);
        message.setAction(action);
        message.putExtra(DConnectMessageService.EXTRA_PACKAGE_NAME, getPackageName(intent));
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
