/*
 Camera2PreviewServerProvider.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.OverlayManager;

import java.util.List;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class Camera2PreviewServerProvider extends AbstractPreviewServerProvider {

    /**
     * プレビュー配信サーバ停止用 Notification の識別子を定義.
     *
     * <p>
     * カメラは、前と後ろが存在するので、BASE_NOTIFICATION_ID + カメラIDのハッシュ値 を識別子とします。
     * </p>
     */
    private static final int BASE_NOTIFICATION_ID = 1001;

    /**
     * オーバーレイを管理するクラス.
     */
    private OverlayManager mOverlayManager;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param recorder レコーダ
     */
    Camera2PreviewServerProvider(final Context context, final Camera2Recorder recorder) {
        super(context, recorder, BASE_NOTIFICATION_ID + recorder.getId().hashCode());

        mContext = context;
        mRecorder = recorder;
        mOverlayManager = new OverlayManager(mContext, recorder);

        HostMediaRecorder.Settings settings = recorder.getSettings();

        addServer(new Camera2MJPEGPreviewServer(context, recorder, settings.getMjpegPort(), false));
        addServer(new Camera2MJPEGPreviewServer(context, recorder, settings.getMjpegSSLPort(), true));
        addServer(new Camera2RTSPPreviewServer(context, recorder, settings.getRtspPort()));
        addServer(new Camera2SRTPreviewServer(context, recorder, settings.getSrtPort()));
    }

    @Override
    public List<PreviewServer> startServers() {
        List<PreviewServer> servers = super.startServers();
        if (!servers.isEmpty()) {
            mOverlayManager.registerBroadcastReceiver();
        }
        return servers;
    }

    @Override
    public void stopServers() {
        mOverlayManager.destroy();
        super.stopServers();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();
        mOverlayManager.onConfigChange();
    }

    @Override
    protected Notification createNotification(PendingIntent pendingIntent, String channelId, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            int iconType = R.drawable.dconnect_icon_lollipop;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext(), channelId);
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title, name));
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message2));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_show), mOverlayManager.createShowActionIntent(mRecorder.getId())).build());
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_hide), mOverlayManager.createHideActionIntent(mRecorder.getId())).build());
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_stop), pendingIntent).build());
            return builder.build();
        } else {
            int iconType = R.drawable.dconnect_icon_lollipop;
            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title, name));
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message2));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.addAction(new Notification.Action.Builder(null,
                    mContext.getString(R.string.overlay_preview_show), mOverlayManager.createShowActionIntent(mRecorder.getId())).build());
            builder.addAction(new Notification.Action.Builder(null,
                    mContext.getString(R.string.overlay_preview_hide), mOverlayManager.createHideActionIntent(mRecorder.getId())).build());
            builder.addAction(new Notification.Action.Builder(null,
                    mContext.getString(R.string.overlay_preview_stop), pendingIntent).build());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
                builder.setChannelId(channelId);
            }
            return builder.build();
        }
    }
}
