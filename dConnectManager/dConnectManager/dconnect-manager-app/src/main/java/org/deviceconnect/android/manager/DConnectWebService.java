/*
 DConnectWebService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;

import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.util.NotificationUtil;
import org.deviceconnect.server.nanohttpd.DConnectWebServerNanoHttpd;

import java.util.logging.Logger;

/**
 * Webサーバ用のサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectWebService extends Service {
    /**
     * ロガー.
     */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * Notification Id.
     */
    private static final int ONGOING_NOTIFICATION_ID = 8080;

    /**
     * Webサーバ.
     */
    private DConnectWebServerNanoHttpd mWebServer;

    /**
     * DConnectの設定.
     */
    private DConnectSettings mSettings;

    /**
     * バインドクラス.
     */
    private final IBinder mLocalBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent) {
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = ((DConnectApplication) getApplication()).getSettings();

        // Webサーバの起動フラグがONになっている場合には起動を行う
        if (mSettings.isWebServerStartFlag()) {
            startWebServer();
        } else {
            NotificationUtil.fakeStartForeground(this,
                    getString(R.string.web_service_on_channel_id),
                    getString(R.string.web_service_on_channel_title),
                    getString(R.string.web_service_on_channel_desc),
                    ONGOING_NOTIFICATION_ID
            );
        }
    }

    @Override
    public void onDestroy() {
        stopWebServer();
        super.onDestroy();
    }

    public int getPort() {
        return mSettings.getWebPort();
    }

    /**
     * Webサーバを起動する.
     */
    public synchronized void startWebServer() {
        if (mWebServer == null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("Web Server was Started.");
                mLogger.info("Host: " + mSettings.getHost());
                mLogger.info("Port: " + mSettings.getWebPort());
                mLogger.info("Document Root: " + mSettings.getDocumentRootPath());
            }

            mWebServer = new DConnectWebServerNanoHttpd.Builder()
                    .port(mSettings.getWebPort())
                    .addDocumentRoot(mSettings.getDocumentRootPath())
                    .cors("*")
                    .version(getVersion(this))
                    .build();
            mWebServer.start();

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mWiFiReceiver, filter);

            showNotification();
        }
    }

    /**
     * Webサーバを停止する.
     */
    public synchronized void stopWebServer() {
        if (mWebServer != null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("Web Server was Stopped.");
            }

            NotificationUtil.hideNotification(this);

            try {
                unregisterReceiver(mWiFiReceiver);
            } catch (Exception e) {
                // ignore.
            }
            mWebServer.stop();
            mWebServer = null;
        }
    }

    /**
     * Web サーバの動作中か確認します.
     *
     * @return 動作中の場合はtrue、それ以外はfalse
     */
    public synchronized boolean isRunning() {
        return mWebServer != null;
    }

    /**
     * Notification に IP アドレスを表示します.
     */
    private void showNotification() {
        NotificationUtil.showNotification(this,
                DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort(),
                getString(R.string.web_service_on_channel_id),
                getString(R.string.service_web_server),
                getString(R.string.web_service_on_channel_title),
                getString(R.string.web_service_on_channel_desc),
                ONGOING_NOTIFICATION_ID);
    }

    /**
     * バージョンコードとバージョン名からバージョンを取得します.
     *
     * @param context コンテキスト
     * @return バージョンの文字列
     */
    private static String getVersion(final Context context) {
        return getVersionName(context) + "_" + getVersionCode(context);
    }

    /**
     * バージョンコードを取得する
     *
     * @param context コンテキスト
     * @return VersionCode
     */
    private static int getVersionCode(final Context context) {
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore.
        }
        return versionCode;
    }

    /**
     * バージョン名を取得する
     *
     * @param context コンテキスト
     * @return VersionName
     */
    private static String getVersionName(final Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore.
        }
        return versionName;
    }

    /**
     * DConnectWebServiceとバインドするためのクラス.
     */
    public class LocalBinder extends Binder {
        /**
         * DConnectWebServiceのインスタンスを取得する.
         *
         * @return DConnectWebServiceのインスタンス
         */
        public DConnectWebService getDConnectWebService() {
            return DConnectWebService.this;
        }
    }

    /**
     * ネットワークiの接続状態の変化を受け取るレシーバー.
     */
    private final BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            showNotification();
        }
    };
}
