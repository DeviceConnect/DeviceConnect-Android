/*
 HostDevicePreviewServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.deviceconnect.android.deviceplugin.host.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

/**
 * Host Device Preview Server.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractPreviewServerProvider implements PreviewServerProvider {
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 通知ID.
     */
    private int mNotificationId;

    /**
     * プレビュー配信サーバーのリスト.
     */
    private final List<PreviewServer> mPreviewServers = new ArrayList<>();

    /**
     * プレビュー配信を行うレコーダ.
     */
    private HostDeviceRecorder mRecorder;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public AbstractPreviewServerProvider(final Context context, final HostDeviceRecorder recorder, final int notificationId) {
        mContext = context;
        mRecorder = recorder;
        mNotificationId = notificationId;
    }

    // PreviewServerProvider

    @Override
    public List<String> getSupportedMimeType() {
        List<String> mimeType = new ArrayList<>();
        for (PreviewServer server : getServers()) {
            mimeType.add(server.getMimeType());
        }
        return mimeType;
    }

    @Override
    public void addServer(PreviewServer server) {
        mPreviewServers.add(server);
    }

    @Override
    public List<PreviewServer> getServers() {
        return mPreviewServers;
    }

    @Override
    public PreviewServer getServerForMimeType(String mimeType) {
        for (PreviewServer server : getServers()) {
            if (server.getMimeType().equalsIgnoreCase(mimeType)) {
                return server;
            }
        }
        return null;
    }

    @Override
    public List<PreviewServer> startServers() {
        List<PreviewServer> results = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(mPreviewServers.size());
        for (PreviewServer server : mPreviewServers) {
            server.startWebServer(new PreviewServer.OnWebServerStartCallback() {
                @Override
                public void onStart(@NonNull String uri) {
                    results.add(server);
                    latch.countDown();
                }

                @Override
                public void onFail() {
                    latch.countDown();
                }
            });
        }

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                // TODO タイムアウト処理
            } else {
                sendNotification(mRecorder.getId(), mRecorder.getName());
            }
        } catch (InterruptedException e) {
            // ignore.
        }
        return results;
    }

    @Override
    public void stopServers() {
        hideNotification(mRecorder.getId());

        for (PreviewServer server : getServers()) {
            server.stopWebServer();
        }
    }

    protected int getDefaultPreviewQuality(String mimeType) {
        return 0;
    }

    public Context getContext() {
        return mContext;
    }

    public void setPreviewQuality(final PreviewServer server, String id, final int quality) {
        server.setQuality(quality);
        storePreviewQuality(server, id, quality);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    protected void storePreviewQuality(final PreviewServer server, String id, int quality) {
        getSharedPreferences().edit().putInt(getPreviewQualityKey(server, id), quality).apply();
    }

    protected int readPreviewQuality(final PreviewServer server, String id) {
        return getSharedPreferences().getInt(getPreviewQualityKey(server, id), getDefaultPreviewQuality(server.getMimeType()));
    }

    private String getPreviewQualityKey(final PreviewServer server, String id) {
        return id + "-" + server.getMimeType() + "-preview-quality";
    }

    /**
     * NotificationIdを取得します.
     *
     * @return NotificationId
     */
    private int getNotificationId() {
        return mNotificationId;
    }

    /**
     * Notificationを削除する.
     *
     * @param id notification を識別する ID
     */
    private void hideNotification(String id) {
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(id, getNotificationId());
        }
    }

    /**
     * Notificationを送信する.
     *
     * @param id notification を識別する ID
     * @param name 名前
     */
    private void sendNotification(String id, String name) {
        PendingIntent contentIntent = createPendingIntent(id);
        Notification notification = createNotification(contentIntent, null, name);
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = mContext.getResources().getString(R.string.overlay_preview_channel_id);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        mContext.getResources().getString(R.string.overlay_preview_content_title),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(mContext.getResources().getString(R.string.overlay_preview_content_message));
                manager.createNotificationChannel(channel);
                notification = createNotification(contentIntent, channelId, name);
            }
            manager.notify(id, getNotificationId(), notification);
        }
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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
            builder.setSmallIcon(R.drawable.dconnect_icon);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title) + " (" + name + ")");
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.dconnect_icon : R.drawable.dconnect_icon_lollipop;
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title) + " (" + name + ")");
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message));
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
     * @param id カメラ ID
     *
     * @return PendingIntent
     */
    private PendingIntent createPendingIntent(String id) {
        Intent intent = new Intent();
        intent.setAction(DELETE_PREVIEW_ACTION);
        intent.putExtra(EXTRA_CAMERA_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }
}
