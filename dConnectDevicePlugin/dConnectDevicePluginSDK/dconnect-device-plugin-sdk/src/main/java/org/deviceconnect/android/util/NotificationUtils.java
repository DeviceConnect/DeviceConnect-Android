/*
 MessageUtils.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.annotation.RequiresApi;

import org.deviceconnect.android.R;

/**
 * 通知(Notification)とActivity起動ユーティリティ
 * @author NTT DOCOMO, INC.
 */
@RequiresApi(Build.VERSION_CODES.Q)
public class NotificationUtils {
    /**
     * Notification Channel Id
     */
    private static final String NOTIFICATION_CHANNEL_ID = "org.deviceconnect.android.util.NotificationUtils";

    /**
     * Notification Channel Name
     */
    private static final String NOTIFICATION_CHANNEL_NAME = "Device Connect Plugin Notification";

    /**
     * Notification Channel Importance
     */
    private static final int NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    /**
     * Notification Content Title
     */
    private static final String NOTIFICATION_CONTENT_TITLE = "Device Connect Plugin";

    /**
     * 通知のチャンネルを作成する
     * @param context コンテキスト
     */
    public static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            if(notificationChannel == null) {
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NOTIFICATION_CHANNEL_IMPORTANCE);
                notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build());
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }
    }

    /**
     * 通知経由でのActivity起動を行う(Notification.Action)
     * @param context コンテキスト
     * @param notificationId 通知のID
     * @param contentText 通知に表示するテキスト
     * @param actions Notification.Actionオブジェクトリスト
     */
    public static void notify(Context context, int notificationId, String contentText, Notification.Action... actions) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            Notification notification = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setActions(actions)
                    .setContentText(contentText)
                    .setContentTitle(NOTIFICATION_CONTENT_TITLE)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_action_labels)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_labels))
                    .setStyle(new Notification.BigTextStyle().setBigContentTitle(NOTIFICATION_CONTENT_TITLE).bigText(contentText))
                    .build();
            notificationManager.notify(notificationId, notification);
        }
    }

    /**
     * 通知経由でのActivity起動を行う(Pending Intent)
     * @param context コンテキスト
     * @param notificationId 通知のID
     * @param requestCode Activity起動のリクエストコード
     * @param intent インテント
     * @param contentText 通知に表示するテキスト
     */
    public static void notify(Context context, int notificationId, int requestCode, Intent intent, String contentText) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentText(contentText)
                    .setContentTitle(NOTIFICATION_CONTENT_TITLE)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_action_labels)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_labels))
                    .setStyle(new Notification.BigTextStyle().setBigContentTitle(NOTIFICATION_CONTENT_TITLE).bigText(contentText))
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(notificationId, notification);
        }
    }

    /**
     * 通知の削除を行う
     * @param context コンテキスト
     * @param id 通知のID
     */
    public static void cancel(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }
}
