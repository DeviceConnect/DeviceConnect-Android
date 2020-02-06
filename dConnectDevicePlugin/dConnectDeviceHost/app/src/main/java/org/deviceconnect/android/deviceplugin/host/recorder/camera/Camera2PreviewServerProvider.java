/*
 Camera2PreviewServerProvider.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.OverlayManager;
import org.deviceconnect.android.deviceplugin.host.recorder.util.OverlayPermissionActivity;

import java.util.List;

import androidx.core.app.NotificationCompat;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class Camera2PreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * プレビュー確認用オーバレイ用アクションを定義.
     */
    private static final String SHOW_OVERLAY_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.SHOW_OVERLAY_PREVIEW";

    /**
     * プレビュー確認用オーバレイ用アクションを定義.
     */
    private static final String HIDE_OVERLAY_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.HIDE_OVERLAY_PREVIEW";

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
     * プレビューを表示する SurfaceView.
     */
    private View mOverlayView;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    /**
     * UI スレッドで動作するハンドラ.
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * プレビュー配信開始フラグ.
     * <p>
     * プレビューが配信中は true、それ以外はfalse
     * </p>
     */
    private boolean mCameraPreviewFlag;

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
        mOverlayManager = new OverlayManager(mContext);

        addServer(new Camera2MJPEGPreviewServer(context, recorder, 11000, mOnEventListener));
        addServer(new Camera2RTSPPreviewServer(context, recorder, 20000, mOnEventListener));
        addServer(new Camera2SRTPreviewServer(context, recorder, 23456, mOnEventListener));
    }

    @Override
    public List<PreviewServer> startServers() {
        List<PreviewServer> servers = super.startServers();
        if (!servers.isEmpty()) {
            registerBroadcastReceiver();
        }
        return servers;
    }

    @Override
    public void stopServers() {
        mCameraPreviewFlag = false;
        unregisterBroadcastReceiver();
        mHandler.post(this::hidePreviewOnOverlay);
        super.stopServers();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();

        if (mOverlayView != null) {
            // 画面が回転したので、オーバーレイのレイアウトも調整
            mOverlayManager.update();
            mOverlayManager.updateView(mOverlayView,
                    0,
                    0,
                    mOverlayManager.getDisplayWidth(),
                    mOverlayManager.getDisplayHeight());

            adjustSurfaceView(mRecorder.isSwappedDimensions());
        }
    }

    @Override
    protected Notification createNotification(PendingIntent pendingIntent, String channelId, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            int iconType = R.drawable.dconnect_icon;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title, name));
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message2));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_show), createShowActionIntent(mRecorder.getId())).build());
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_hide), createHideActionIntent(mRecorder.getId())).build());
            builder.addAction(new NotificationCompat.Action.Builder(iconType,
                    mContext.getString(R.string.overlay_preview_stop), pendingIntent).build());
            return builder.build();
        } else {
            int iconType = R.drawable.dconnect_icon_lollipop;
            Icon icon = Icon.createWithResource(mContext, R.drawable.dconnect_icon_lollipop);
            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title, name));
            builder.setContentText(mContext.getString(R.string.overlay_preview_content_message2));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.addAction(new Notification.Action.Builder(icon,
                    mContext.getString(R.string.overlay_preview_show), createShowActionIntent(mRecorder.getId())).build());
            builder.addAction(new Notification.Action.Builder(icon,
                    mContext.getString(R.string.overlay_preview_hide), createHideActionIntent(mRecorder.getId())).build());
            builder.addAction(new Notification.Action.Builder(icon,
                    mContext.getString(R.string.overlay_preview_stop), pendingIntent).build());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
                builder.setChannelId(channelId);
            }
            return builder.build();
        }
    }

    /**
     * 画面にプレビューを表示するための PendingIntent を作成します.
     * @param id カメラID
     * @return PendingIntent
     */
    private PendingIntent createShowActionIntent(String id) {
        Intent intent = new Intent();
        intent.setAction(SHOW_OVERLAY_PREVIEW_ACTION);
        intent.putExtra(EXTRA_CAMERA_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }

    /**
     * 画面にプレビューを非表示にするための PendingIntent を作成します.
     *
     * @param id カメラID
     * @return PendingIntent
     */
    private PendingIntent createHideActionIntent(String id) {
        Intent intent = new Intent();
        intent.setAction(HIDE_OVERLAY_PREVIEW_ACTION);
        intent.putExtra(EXTRA_CAMERA_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }

    /**
     * 各 PreviewServer に対して、カメラの再起動要求を送信します.
     */
    private void restartCamera() {
        for (PreviewServer server : getServers()) {
            ((Camera2PreviewServer) server).restartCamera();
        }
    }

    /**
     * オーバーレイの許可を求めるための Activity を開きます.
     */
    private void openOverlayPermissionActivity() {
        Intent intent = new Intent();
        intent.setClass(mContext, OverlayPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * オーバーレイ上にプレビューを表示します.
     */
    private synchronized void showPreviewOnOverlay() {
        if (mOverlayView != null) {
            return;
        }

        // Notification を閉じるイベントを送信
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if (!mOverlayManager.isOverlayAllowed()) {
            openOverlayPermissionActivity();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);

        mOverlayView = inflater.inflate(R.layout.host_preview_overlay, null);

        SurfaceView surfaceView = mOverlayView.findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mRecorder.setTargetSurface(surfaceView.getHolder().getSurface());

                if (mCameraPreviewFlag) {
                    // 既にプレビューが配信中の場合は、オーバーレイ用の Surface を追加してから
                    // カメラを再起動させます。
                    restartCamera();
                } else {
                    new Thread(() -> {
                        try {
                            mRecorder.startPreview(null);
                        } catch (CameraWrapperException e) {
                            // ignore.
                        }
                    }).start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });

        mOverlayManager.addView(mOverlayView,
                0,
                0,
                mOverlayManager.getDisplayWidth(),
                mOverlayManager.getDisplayHeight(),
                "overlay-" + mRecorder.getId());

        adjustSurfaceView(mRecorder.isSwappedDimensions());
    }

    /**
     * オーバーレイ上に表示しているプレビューを削除します.
     */
    private synchronized void hidePreviewOnOverlay() {
        if (mOverlayView != null) {
            try {
                mRecorder.setTargetSurface(null);
                mRecorder.stopPreview();
            } catch (CameraWrapperException e) {
                // ignore.
            }
            mOverlayManager.removeAllViews();
            mOverlayView = null;

            // プレビュー配信中は、カメラを再開させます。
            if (mCameraPreviewFlag) {
                restartCamera();
            }
        }
    }

    /**
     * プレビューのサイズを View に収まるように調整します.
     *
     * @param isSwappedDimensions 縦横の幅をスワップする場合はtrue、それ以外はfalse
     */
    private synchronized void adjustSurfaceView(boolean isSwappedDimensions) {
        if (mOverlayView == null) {
            return;
        }

        mHandler.post(() -> {
            HostMediaRecorder.PictureSize previewSize = mRecorder.getPreviewSize();
            int cameraWidth = previewSize.getWidth();
            int cameraHeight = previewSize.getHeight();

            SurfaceView surfaceView = mOverlayView.findViewById(R.id.surface_view);
            Size changeSize;
            Size viewSize = new Size(mOverlayManager.getDisplayWidth(), mOverlayManager.getDisplayHeight());
            if (isSwappedDimensions) {
                changeSize = calculateViewSize(cameraHeight, cameraWidth, viewSize);
            } else {
                changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);
            }

            mOverlayManager.updateView(mOverlayView,
                    0,
                    0,
                    changeSize.getWidth(),
                    changeSize.getHeight());

            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        });
    }

    /**
     * viewSize のサイズに収まるように width と height の値を計算します.
     *
     * @param width    横幅
     * @param height   縦幅
     * @param viewSize Viewのサイズ
     * @return viewSize に収まるように計算された縦横のサイズ
     */
    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h = (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }

    /**
     * 画面にプレビューを表示するためのアクションを受け取るための BroadcastReceiver を登録します.
     */
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_OVERLAY_PREVIEW_ACTION);
        filter.addAction(HIDE_OVERLAY_PREVIEW_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 画面にプレビューを表示するためのアクションを受け取るための BroadcastReceiver を解除します.
     */
    private void unregisterBroadcastReceiver() {
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * 画面にプレビューを表示するためのアクションを受け取るための BroadcastReceiver.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            if (SHOW_OVERLAY_PREVIEW_ACTION.equals(action)) {
                mHandler.post(() -> showPreviewOnOverlay());
            } else if (HIDE_OVERLAY_PREVIEW_ACTION.equals(action)) {
                mHandler.post(() -> hidePreviewOnOverlay());
            }
        }
    };

    /**
     * カメラからのイベントを受け取ります.
     */
    private final Camera2PreviewServer.OnEventListener mOnEventListener = new Camera2PreviewServer.OnEventListener() {
        @Override
        public void onCameraStarted() {
            mCameraPreviewFlag = true;

            // プレビュー配信が開始されるので、オーバーレイに表示していたカメラを停止します。
            try {
                mRecorder.stopPreview();
            } catch (CameraWrapperException e) {
                // ignore.
            }

            if (mOverlayView != null) {
                SurfaceView surfaceView = mOverlayView.findViewById(R.id.surface_view);
                mRecorder.setTargetSurface(surfaceView.getHolder().getSurface());
                adjustSurfaceView(mRecorder.isSwappedDimensions());
            }
        }

        @Override
        public void onCameraStopped() {
            mCameraPreviewFlag = false;

            if (mOverlayView != null) {
                // カメラの停止は、非同期で行われるので、ここでは、カメラの再開処理を 500msec まってから行います。
                mHandler.postDelayed(() -> {
                    if (mOverlayView != null) {
                        SurfaceView surfaceView = mOverlayView.findViewById(R.id.surface_view);
                        new Thread(() -> {
                            try {
                                mRecorder.setTargetSurface(surfaceView.getHolder().getSurface());
                                mRecorder.startPreview(null);
                            } catch (CameraWrapperException e) {
                                // ignore.
                            }
                        }).start();
                        adjustSurfaceView(mRecorder.isSwappedDimensions());
                    }
                }, 500);
            }
        }
    };
}
