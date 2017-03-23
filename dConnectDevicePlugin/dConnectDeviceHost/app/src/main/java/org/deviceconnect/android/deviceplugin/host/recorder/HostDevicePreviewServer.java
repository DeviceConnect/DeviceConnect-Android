/*
 HostDevicePreviewServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.R;

/**
 * Host Device Preview Server.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class HostDevicePreviewServer implements HostDeviceRecorder {

    /**
     * オーバーレイ削除用アクションを定義.
     */
    public static final String DELETE_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.DELETE_PREVIEW";

    /**
     * カメラを識別するIDのキー名を定義.
     */
    public static final String EXTRA_CAMERA_ID = "cameraId";

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 通知ID.
     */
    private int mNotificationId;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param notificationId 通知ID
     */
    public HostDevicePreviewServer(final Context context, final int notificationId) {
        mContext = context;
        mNotificationId = notificationId;
    }

    /**
     * サーバを開始します.
     * @param callback 開始結果を通知するコールバック
     */
    public abstract void startWebServer(OnWebServerStartCallback callback);

    /**
     * サーバを停止します.
     */
    public abstract void stopWebServer();

    /**
     * NotificationIdを取得します.
     * @return NotificationId
     */
    private int getNotificationId() {
        return mNotificationId;
    }

    /**
     * Notificationを削除する.
     */
    public void hideNotification() {
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(getId(), getNotificationId());
    }

    /**
     * Notificationを送信する.
     */
    public void sendNotification() {
        PendingIntent contentIntent = createPendingIntent();
        Notification notification = createNotification(contentIntent);
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        manager.notify(getId(), getNotificationId(), notification);
    }

    /**
     * Notificationを作成する.
     * @param pendingIntent Notificationがクリックされたときに起動するIntent
     * @return Notification
     */
    private Notification createNotification(final PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title) + " (" + getName() + ")");
        builder.setContentText(mContext.getString(R.string.overlay_preview_content_message));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        return builder.build();
    }

    /**
     * PendingIntentを作成する.
     * @return PendingIntent
     */
    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(mContext, HostDeviceService.class);
        intent.setAction(DELETE_PREVIEW_ACTION);
        intent.putExtra(EXTRA_CAMERA_ID, getId());
        return PendingIntent.getService(mContext, getNotificationId(), intent, 0);
    }

    /**
     * Callback interface used to receive the result of starting a web server.
     */
    public interface OnWebServerStartCallback {
        /**
         * Called when a web server successfully started.
         *
         * @param uri An ever-updating, static image URI.
         */
        void onStart(String uri);

        /**
         * Called when a web server failed to start.
         */
        void onFail();
    }
}
