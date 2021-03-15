package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

public abstract class AbstractMediaRecorder implements MediaRecorder {
    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * リクエストの処理を実行するハンドラ.
     */
    private final Handler mRequestHandler;

    /**
     * イベント通知用リスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * レコーダの状態.
     */
    private State mState = State.INACTIVE;

    /**
     * コンストラクタ.
     */
    public AbstractMediaRecorder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }

        mContext = context;

        HandlerThread requestThread = new HandlerThread("uvc-media-recorder");
        requestThread.start();
        mRequestHandler = new Handler(requestThread.getLooper());
    }

    // Implements MediaRecorder methods.

    @Override
    public void initialize() {
        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.setOnEventListener(new BroadcasterProvider.OnEventListener() {
                @Override
                public void onStarted(Broadcaster broadcaster) {
                    postOnBroadcasterStarted(broadcaster);
                }

                @Override
                public void onStopped(Broadcaster broadcaster) {
                    postOnBroadcasterStopped(broadcaster);
                }

                @Override
                public void onError(Broadcaster broadcaster, Exception e) {
                    postOnBroadcasterError(broadcaster, e);
                }
            });
        }

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.setOnEventListener(new PreviewServerProvider.OnEventListener() {
                @Override
                public void onStarted(List<PreviewServer> servers) {
                    postOnPreviewStarted(servers);
                }

                @Override
                public void onStopped() {
                    postOnPreviewStopped();
                }

                @Override
                public void onError(PreviewServer server, Exception e) {
                    postOnPreviewError(e);
                }
            });
        }
    }

    @Override
    public void clean() {
        try {
            BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
            if (broadcasterProvider != null) {
                broadcasterProvider.stopBroadcaster();
            }

            PreviewServerProvider serverProvider = getServerProvider();
            if (serverProvider != null) {
                serverProvider.stopServers();;
            }

            EGLSurfaceDrawingThread thread = getSurfaceDrawingThread();
            if (thread != null) {
                thread.stop();
            }
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public void destroy() {
        clean();
        mRequestHandler.getLooper().quit();
    }

    @Override
    public void onConfigChange() {
        PreviewServerProvider serverProvider = getServerProvider();
        if (serverProvider != null) {
            serverProvider.onConfigChange();
        }

        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.onConfigChange();
        }

        postOnConfigChanged();
    }

    @Override
    public State getState() {
        return mState;
    }

    @Override
    public boolean isPreviewRunning() {
        PreviewServerProvider provider = getServerProvider();
        return provider != null && provider.isRunning();
    }

    @Override
    public List<PreviewServer> startPreview() {
        PreviewServerProvider provider = getServerProvider();
        if (provider == null) {
            return new ArrayList<>();
        }

        List<PreviewServer> servers = provider.startServers();
        if (!servers.isEmpty()) {
            provider.setMute(getSettings().isMute());
        }
        return servers;
    }

    @Override
    public void stopPreview() {
        PreviewServerProvider provider = getServerProvider();
        if (provider != null) {
            provider.stopServers();
        }
    }

    @Override
    public boolean isBroadcasterRunning() {
        BroadcasterProvider provider = getBroadcasterProvider();
        return provider != null && provider.isRunning();
    }

    @Override
    public Broadcaster startBroadcaster(String uri) {
        if (uri == null) {
            return null;
        }

        BroadcasterProvider provider = getBroadcasterProvider();
        if (provider == null) {
            return null;
        }

        Broadcaster broadcaster = provider.startBroadcaster(uri);
        if (broadcaster != null) {
            broadcaster.setMute(getSettings().isMute());
        }
        return broadcaster;
    }

    @Override
    public void stopBroadcaster() {
        BroadcasterProvider provider = getBroadcasterProvider();
        if (provider != null) {
            provider.stopBroadcaster();
        }
    }

    @Override
    public void setMute(boolean mute) {
        Settings settings = getSettings();
        settings.setMute(mute);

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.setMute(mute);
        }

        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.setMute(mute);
        }
    }

    @Override
    public boolean isMute() {
        return getSettings().isMute();
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {
        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            for (PreviewServer server : previewProvider.getServers()) {
                server.setSSLContext(sslContext);
            }
        }
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public long getBPS() {
        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider == null) {
            return 0;
        }

        long bps = 0;
        List<PreviewServer> servers = previewProvider.getServers();
        for (PreviewServer previewServer : servers) {
            bps += previewServer.getBPS();
        }
        return bps;
    }

    /**
     * Runnable を順番に実行します.
     *
     * @param run 実行する Runnable
     */
    protected void postRequestHandler(Runnable run) {
        mRequestHandler.post(run);
    }

    /**
     * レコーダの状態を設定します.
     *
     * @param state レコーダの状態
     */
    protected void setState(State state) {
        mState = state;
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * パーミッション要求します.
     *
     * @param permissions パーミッションのリスト
     * @param callback パーミションの結果を受け取るコールバック
     */
    protected void requestPermission(String[] permissions, PermissionCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        PermissionUtility.requestPermissions(getContext(), handler, permissions, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onAllowed();
            }

            @Override
            public void onFail(@NonNull String deniedPermission) {
                callback.onDisallowed();
            }
        });
    }

    /**
     * NotificationId を取得します.
     *
     * @return NotificationId
     */
    private int getNotificationId() {
        return 2000 + getId().hashCode();
    }

    /**
     * レコーディング停止用の Notification を削除します.
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
     * 録音・録画停止用の Notification を表示します.
     *
     * @param id notification を識別する ID
     * @param name 名前
     */
    private void showNotificationForStopRecording(String id, String name) {
        PendingIntent contentIntent = createPendingIntentForStopRecording(id);
        Notification notification = createNotificationForStopRecording(contentIntent, null, name);
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = mContext.getResources().getString(R.string.uvc_notification_preview_channel_id);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        mContext.getResources().getString(R.string.uvc_notification_recorder_recording),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(mContext.getResources().getString(R.string.uvc_notification_recorder_recording_content));
                manager.createNotificationChannel(channel);
                notification = createNotificationForStopRecording(contentIntent, channelId, name);
            }
            manager.notify(id, getNotificationId(), notification);
        }
    }

    /**
     * 録音・録画停止用の Notification を作成する.
     *
     * @param pendingIntent Notificationがクリックされたときに起動する Intent
     * @param channelId チャンネルID
     * @param name 名前
     * @return Notification
     */
    protected Notification createNotificationForStopRecording(final PendingIntent pendingIntent, final String channelId, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.uvc_notification_recorder_recording_ticker));
            builder.setSmallIcon(R.drawable.dconnect_icon);
            builder.setContentTitle(mContext.getString(R.string.uvc_notification_recorder_recording, name));
            builder.setContentText(mContext.getString(R.string.uvc_notification_recorder_recording_content));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.uvc_notification_preview_ticker));
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.dconnect_icon : R.drawable.dconnect_icon_lollipop;
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.uvc_notification_recorder_recording, name));
            builder.setContentText(mContext.getString(R.string.uvc_notification_recorder_recording_content));
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
    private PendingIntent createPendingIntentForStopRecording(String id) {
        Intent intent = new Intent();
        intent.setAction(MediaRecorderManager.ACTION_STOP_RECORDING);
        intent.putExtra(MediaRecorderManager.KEY_RECORDER_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }

    protected void postOnConfigChanged() {
        if (mOnEventListener != null) {
            mOnEventListener.onConfigChanged();
        }
    }

    protected void postOnPreviewStarted(List<PreviewServer> servers) {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewStarted(servers);
        }
    }

    protected void postOnPreviewStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewStopped();
        }
    }

    protected void postOnPreviewError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewError(e);
        }
    }

    protected void postOnBroadcasterStarted(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterStarted(broadcaster);
        }
    }

    protected void postOnBroadcasterStopped(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterStopped(broadcaster);
        }
    }

    protected void postOnBroadcasterError(Broadcaster broadcaster, Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterError(broadcaster, e);
        }
    }

    protected void postOnError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }
}