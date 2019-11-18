package org.deviceconnect.android.manager.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.setting.SettingActivity;

public final class NotificationUtil {

    private NotificationUtil() {}

    /**
     * サービスをフォアグランドに設定する。
     */
    public static void showNotification(final Service service,
                                        final String uri,
                                        final String chnannelId,
                                        final String ticker,
                                        final String title,
                                        final String description,
                                        final int notificationId) {
        Intent notificationIntent = new Intent(service.getApplicationContext(), SettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                service.getApplicationContext(), 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(service.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(ticker);
            builder.setContentTitle(title);
            builder.setContentText(uri);
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.icon : R.drawable.on_icon;
            builder.setSmallIcon(iconType);

            service.startForeground(notificationId, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(service.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(ticker);
            builder.setContentTitle(title);
            builder.setContentText(uri);
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.icon : R.drawable.on_icon;
            builder.setSmallIcon(iconType);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = chnannelId;
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        title,
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(description);
                NotificationManager mNotification = (NotificationManager) service.getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotification.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            service.startForeground(notificationId, builder.build());
        }
    }

    /**
     * DConnectServiceがOFF時にstartForegroundService()が行われた時にキャンセルする.
     */
    public static void fakeStartForeground(final Service service,
                                           final String channelId,
                                           final String title,
                                           final String description,
                                           final int notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(service.getApplicationContext(),
                                    channelId)
                    .setContentTitle("").setContentText("");
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    title,
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.icon : R.drawable.on_icon;
            builder.setSmallIcon(iconType);
            NotificationManager mNotification = (NotificationManager) service.getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.createNotificationChannel(channel);
            builder.setChannelId(channelId);
            service.startForeground(notificationId, builder.build());
            service.stopForeground(true);
            service.stopSelf();
        }
    }

    /**
     * フォアグランドを停止する。
     */
    public static void hideNotification(final Service service) {
        service.stopForeground(true);
    }
}
