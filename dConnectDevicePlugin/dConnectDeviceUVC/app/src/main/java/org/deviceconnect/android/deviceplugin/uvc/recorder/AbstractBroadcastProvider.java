package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractBroadcastProvider implements BroadcasterProvider {
    /**
     * 映像を配信するクラス.
     */
    private Broadcaster mBroadcaster;

    /**
     * イベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    private final Context mContext;
    private final MediaRecorder mRecorder;

    public AbstractBroadcastProvider(Context context, MediaRecorder recorder) {
        mContext = context;
        mRecorder = recorder;
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public Broadcaster getBroadcaster() {
        return mBroadcaster;
    }

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }

    @Override
    public Broadcaster startBroadcaster(String broadcastURI) {
        if (broadcastURI == null) {
            return null;
        }

        if (mBroadcaster != null && mBroadcaster.isRunning()) {
            return mBroadcaster;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);

        mBroadcaster = createBroadcaster(broadcastURI);
        if (mBroadcaster == null) {
            return null;
        }
        mBroadcaster.setOnEventListener(new Broadcaster.OnEventListener() {
            @Override
            public void onStarted() {
                postBroadcastStarted(mBroadcaster);
            }

            @Override
            public void onStopped() {
                hideNotification(mRecorder.getId());
                postBroadcastStopped(mBroadcaster);
            }

            @Override
            public void onError(Exception e) {
                postBroadcastError(mBroadcaster, e);
            }
        });

        mBroadcaster.start(new Broadcaster.OnStartCallback() {
            @Override
            public void onSuccess() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onFailed(Exception e) {
                result.set(false);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }

        if (!result.get()) {
            mBroadcaster.stop();
            mBroadcaster = null;
        } else {
            sendNotification(mRecorder.getId(), mRecorder.getName());
        }

        return mBroadcaster;
    }

    @Override
    public void stopBroadcaster() {
        hideNotification(mRecorder.getId());

        if (mBroadcaster != null) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }

    @Override
    public void onConfigChange() {
        if (mBroadcaster != null) {
            mBroadcaster.onConfigChange();
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mBroadcaster != null) {
            mBroadcaster.setMute(mute);
        }
    }

    /**
     * Broadcaster のインスタンスを作成します.
     *
     * @param broadcastURI 配信先の URI
     * @return Broadcaster のインスタンス
     */
    public abstract Broadcaster createBroadcaster(String broadcastURI);

    private void postBroadcastStarted(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted(broadcaster);
        }
    }

    private void postBroadcastStopped(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped(broadcaster);
        }
    }

    private void postBroadcastError(Broadcaster broadcaster, Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(broadcaster, e);
        }
    }

    /**
     * Notification の Id を取得します.
     *
     * @return Notification の Id
     */
    private int getNotificationId() {
        return 1000 + mRecorder.getId().hashCode();
    }

    /**
     * プレビュー配信サーバ停止用の Notification を削除します.
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
     * プレビュー配信サーバ停止用の Notification を送信します.
     *
     * @param id notification を識別する ID
     * @param name 名前
     */
    private void sendNotification(String id, String name) {
//        PendingIntent contentIntent = createPendingIntent(id);
//        Notification notification = createNotification(contentIntent, null, name);
//        NotificationManager manager = (NotificationManager) mContext
//                .getSystemService(Service.NOTIFICATION_SERVICE);
//        if (manager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                String channelId = mContext.getResources().getString(R.string.overlay_preview_channel_id);
//                NotificationChannel channel = new NotificationChannel(
//                        channelId,
//                        mContext.getResources().getString(R.string.host_notification_recorder_broadcast),
//                        NotificationManager.IMPORTANCE_LOW);
//                channel.setDescription(mContext.getResources().getString(R.string.host_notification_recorder_broadcast_content));
//                manager.createNotificationChannel(channel);
//                notification = createNotification(contentIntent, channelId, name);
//            }
//            manager.notify(id, getNotificationId(), notification);
//        }
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
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
//            builder.setContentIntent(pendingIntent);
//            builder.setTicker(mContext.getString(R.string.host_notification_recorder_broadcast_ticker));
//            builder.setSmallIcon(R.drawable.dconnect_icon);
//            builder.setContentTitle(mContext.getString(R.string.host_notification_recorder_broadcast, name));
//            builder.setContentText(mContext.getString(R.string.host_notification_recorder_broadcast_content));
//            builder.setWhen(System.currentTimeMillis());
//            builder.setAutoCancel(true);
//            builder.setOngoing(true);
//            return builder.build();
//        } else {
//            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
//            builder.setContentIntent(pendingIntent);
//            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
//            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
//                    R.drawable.dconnect_icon : R.drawable.dconnect_icon_lollipop;
//            builder.setSmallIcon(iconType);
//            builder.setContentTitle(mContext.getString(R.string.host_notification_recorder_broadcast, name));
//            builder.setContentText(mContext.getString(R.string.host_notification_recorder_broadcast_content));
//            builder.setWhen(System.currentTimeMillis());
//            builder.setAutoCancel(true);
//            builder.setOngoing(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
//                builder.setChannelId(channelId);
//            }
//            return builder.build();
//        }
        return null;
    }

    /**
     * PendingIntent を作成する.
     *
     * @param id レコーダ ID
     *
     * @return PendingIntent
     */
    private PendingIntent createPendingIntent(String id) {
//        Intent intent = new Intent();
//        intent.setAction(MediaRecorderManager.ACTION_STOP_BROADCAST);
//        intent.putExtra(MediaRecorderManager.KEY_RECORDER_ID, id);
//        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
        return null;
    }
}
