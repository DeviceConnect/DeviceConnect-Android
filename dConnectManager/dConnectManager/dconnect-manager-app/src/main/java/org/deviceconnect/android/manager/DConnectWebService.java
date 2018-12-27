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
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.util.NotificationUtil;
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;

import java.util.logging.Logger;

/**
 * Webサーバ用のサービス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectWebService extends Service {
    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");
    /** Notification Id. */
    private static final int ONGOING_NOTIFICATION_ID = 8080;

    /** Webサーバ. */
    private DConnectServer mWebServer;

    /** DConnectの設定. */
    private DConnectSettings mSettings;

    /** バインドクラス. */
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

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        return START_STICKY;
    }

    /**
     * Webサーバを起動する.
     */
    public synchronized void startWebServer() {
        if (mWebServer == null) {
            DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
            builder.port(mSettings.getWebPort())
                    .documentRootPath(mSettings.getDocumentRootPath());

            if (BuildConfig.DEBUG) {
                mLogger.info("Web Server was Started.");
                mLogger.info("Host: " + mSettings.getHost());
                mLogger.info("Port: " + mSettings.getWebPort());
                mLogger.info("Document Root: " + mSettings.getDocumentRootPath());
            }

            mWebServer = new DConnectServerNanoHttpd(builder.build(), this, null);
            mWebServer.start();
            NotificationUtil.showNotification(this,
                    DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort(),
                    getString(R.string.web_service_on_channel_id),
                    getString(R.string.service_web_server),
                    getString(R.string.web_service_on_channel_title),
                    getString(R.string.web_service_on_channel_desc),
                    ONGOING_NOTIFICATION_ID
                    );

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mWiFiReceiver, filter);
        }
    }

    /**
     * Webサーバを停止する.
     */
    public synchronized void stopWebServer() {
        if (mWebServer != null) {
            unregisterReceiver(mWiFiReceiver);
            mWebServer.shutdown();
            mWebServer = null;
            NotificationUtil.hideNotification(this);
        }
        if (BuildConfig.DEBUG) {
            mLogger.info("Web Server was Stopped.");
        }
    }

    public synchronized boolean isRunning() {
        return mWebServer != null;
    }

    /**
     * フォアグランドを停止する。
     */
    private void hideNotification() {
        stopForeground(true);
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
            NotificationUtil.showNotification(DConnectWebService.this,
                    DConnectUtil.getIPAddress(DConnectWebService.this) + ":" + mSettings.getWebPort(),
                    getString(R.string.web_service_on_channel_id),
                    getString(R.string.service_web_server),
                    getString(R.string.web_service_on_channel_title),
                    getString(R.string.web_service_on_channel_desc),
                    ONGOING_NOTIFICATION_ID
            );
        }
    };
}
