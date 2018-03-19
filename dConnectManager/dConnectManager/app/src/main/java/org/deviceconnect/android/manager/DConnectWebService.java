/*
 DConnectWebService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.android.manager.util.DConnectUtil;
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
            fakeStartForeground();
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
     * WebServerがOFF時にstartForegroundService()が行われた時にキャンセルする.
     */
    private void fakeStartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, getString(R.string.web_service_on_channel_id))
                    .setContentTitle("").setContentText("");
            startForeground(ONGOING_NOTIFICATION_ID, builder.build());
            stopForeground(true);
            stopSelf();
        }
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
            showNotification();

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
            hideNotification();
        }
        if (BuildConfig.DEBUG) {
            mLogger.info("Web Server was Stopped.");
        }
    }

    public synchronized boolean isRunning() {
        return mWebServer != null;
    }

    /**
     * サービスをフォアグランドに設定する。
     */
    private void showNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), SettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(getString(R.string.service_web_server));
            builder.setContentTitle(getString(R.string.service_web_server));
            builder.setContentText(DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort());
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.icon : R.drawable.on_icon;
            builder.setSmallIcon(iconType);

            startForeground(ONGOING_NOTIFICATION_ID, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(getString(R.string.service_web_server));
            builder.setContentTitle(getString(R.string.service_web_server));
            builder.setContentText(DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort());
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.icon : R.drawable.on_icon;
            builder.setSmallIcon(iconType);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = getString(R.string.web_service_on_channel_id);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        getString(R.string.web_service_on_channel_title),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(getString(R.string.web_service_on_channel_desc));
                NotificationManager mNotification = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotification.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            startForeground(ONGOING_NOTIFICATION_ID, builder.build());
        }
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
            showNotification();
        }
    };
}
