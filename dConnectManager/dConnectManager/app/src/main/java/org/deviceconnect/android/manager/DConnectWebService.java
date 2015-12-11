/*
 DConnectWebService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import org.deviceconnect.android.activity.PermissionUtility;
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

    /** Handler of permission. */
    private final Handler mHandler = new Handler();

    @Override
    public IBinder onBind(final Intent intent) {
        return (IBinder) mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = DConnectSettings.getInstance();
        mSettings.load(this);
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
    private synchronized void startWebServer() {
        if (mWebServer == null) {
            mSettings.load(this);

            DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
            builder.port(mSettings.getWebPort())
                    .documentRootPath(mSettings.getDocumentRootPath());

            if (BuildConfig.DEBUG) {
                mLogger.info("Web Server was Started.");
                mLogger.info("Host: " + mSettings.getHost());
                mLogger.info("Port: " + mSettings.getWebPort());
                mLogger.info("Document Root: " + mSettings.getDocumentRootPath());
            }

            mWebServer = new DConnectServerNanoHttpd(builder.build(), this);
            mWebServer.start();
            showNotification();
        }
    }

    /**
     * Webサーバを停止する.
     */
    private synchronized void stopWebServer() {
        if (mWebServer != null) {
            mWebServer.shutdown();
            mWebServer = null;
            hideNotification();
        }
        if (BuildConfig.DEBUG) {
            mLogger.info("Web Server was Stopped.");
        }
    }

    /**
     * サービスをフォアグランドに設定する。
     */
    private void showNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), SettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker(getString(R.string.service_web_server));
        builder.setContentTitle(getString(R.string.service_web_server));
        builder.setContentText(DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort());
        int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                R.drawable.icon : R.drawable.on_icon;

        builder.setSmallIcon(iconType);

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    /**
     * フォアグランドを停止する。
     */
    private void hideNotification() {
        stopForeground(true);
    }

    private final IDConnectWebService mBinder = new IDConnectWebService.Stub()  {
        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return mWebServer != null;
        }

        @Override
        public void start() throws RemoteException {
            if (DConnectUtil.isPermission(DConnectWebService.this)) {
                startWebServer();
            } else {
                PermissionUtility.requestPermissions(DConnectWebService.this,
                        mHandler,
                        DConnectUtil.PERMISSIONS,
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                startWebServer();
                            }
                            @Override
                            public void onFail(final String deniedPermission) {
                                mLogger.warning("Denied Permission. " + deniedPermission);
                            }
                        });
            }
        }

        @Override
        public void stop() throws RemoteException {
            stopWebServer();
        }
    };
}
