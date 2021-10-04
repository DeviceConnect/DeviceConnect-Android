package org.deviceconnect.android.deviceplugin.host.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.deviceplugin.host.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBroadcastProvider extends AbstractLiveStreamingProvider implements BroadcasterProvider {

    public AbstractBroadcastProvider(Context context, HostMediaRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public List<String> getSupportedMimeType() {
        List<String> mimeType = new ArrayList<>();
        for (LiveStreaming server : getLiveStreamingList()) {
            mimeType.add(server.getMimeType());
        }
        return mimeType;
    }

    @Override
    protected void hideNotification(String id) {
        NotificationManager manager = (NotificationManager) getContext()
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(id, getNotificationId());
        }
    }

    @Override
    protected void sendNotification(String id, String name) {
        PendingIntent contentIntent = createPendingIntent(id);
        Notification notification = createNotification(contentIntent, null, name);
        NotificationManager manager = (NotificationManager) getContext()
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = getContext().getResources().getString(R.string.overlay_preview_channel_id);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        getContext().getResources().getString(R.string.host_notification_recorder_broadcast),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(getContext().getResources().getString(R.string.host_notification_recorder_broadcast_content));
                manager.createNotificationChannel(channel);
                notification = createNotification(contentIntent, channelId, name);
            }
            manager.notify(id, getNotificationId(), notification);
        }
    }

    /**
     * Notification の Id を取得します.
     *
     * @return Notification の Id
     */
    private int getNotificationId() {
        return 1000 + getRecorder().getId().hashCode();
    }

    /**
     * Notificationを作成する.
     *
     * @param pendingIntent Notificationがクリックされたときに起動する Intent
     * @param channelId チャンネルID
     * @param name 名前
     * @return Notification
     */
    private Notification createNotification(final PendingIntent pendingIntent, final String channelId, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext().getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(getContext().getString(R.string.host_notification_recorder_broadcast_ticker));
            builder.setSmallIcon(R.drawable.dconnect_icon);
            builder.setContentTitle(getContext().getString(R.string.host_notification_recorder_broadcast, name));
            builder.setContentText(getContext().getString(R.string.host_notification_recorder_broadcast_content));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(getContext().getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(getContext().getString(R.string.overlay_preview_ticker));
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.dconnect_icon : R.drawable.dconnect_icon_lollipop;
            builder.setSmallIcon(iconType);
            builder.setContentTitle(getContext().getString(R.string.host_notification_recorder_broadcast, name));
            builder.setContentText(getContext().getString(R.string.host_notification_recorder_broadcast_content));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
                builder.setChannelId(channelId);
            }
            return builder.build();
        }
    }

    /**
     * PendingIntent を作成する.
     *
     * @param id レコーダ ID
     *
     * @return PendingIntent
     */
    private PendingIntent createPendingIntent(String id) {
        Intent intent = new Intent();
        intent.setAction(HostMediaRecorderManager.ACTION_STOP_BROADCAST);
        intent.putExtra(HostMediaRecorderManager.KEY_RECORDER_ID, id);
        return PendingIntent.getBroadcast(getContext(), getNotificationId(), intent, 0);
    }
}
