package org.deviceconnect.android.manager.protection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.manager.R;

import static org.deviceconnect.android.manager.BuildConfig.DEBUG;

/**
 * 画面キャプチャを禁止するためのオーバーレイ.
 */
class ScreenRecordingGuardOverlay extends CopyGuardSetting {

    static final String ACTION_PERMISSION_RESULT = "org.deviceconnect.android.manager.action.PERMISSION_RESULT";

    private static final String ACTION_CAPTURE_GUARD_OFF = "org.deviceconnect.android.manager.action.CAPTURE_GUARD_OFF";

    private static final String TAG = "CaptureGuardOverlay";

    private final Context mContext;

    private final WindowManager mWindowManager;

    private final WindowManager.LayoutParams mLayoutParams = createLayoutParams();

    private final View mOverlayView;

    private final Handler mMainHandler;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (ACTION_PERMISSION_RESULT.equals(action)) {
                if (isOverlayAllowed()) {
                    showInternal();
                }
            } else if (ACTION_CAPTURE_GUARD_OFF.equals(action)) {
                disable();
            }
        }
    };

    private boolean mIsShown;

    private final int mAppIconId;

    ScreenRecordingGuardOverlay(final Context context,
                                final int appIconId) {
        mContext = context;
        mAppIconId = appIconId;
        registerReceiver(context);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mOverlayView = inflater.inflate(R.layout.copy_guard_overlay, null);
        mOverlayView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(final View view) {
                if (DEBUG) {
                    Log.d(TAG, "Overlay is attached to window.");
                }
                notifyOnSettingChange(true);
                showNotification();
            }

            @Override
            public void onViewDetachedFromWindow(final View view) {
                if (DEBUG) {
                    Log.d(TAG, "Overlay is detach from window.");
                }
                hideNotification();
                notifyOnSettingChange(false);
            }
        });

        mMainHandler = new Handler(Looper.getMainLooper());
    }

    void release() {
        hideNotification();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void registerReceiver(final Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PERMISSION_RESULT);
        intentFilter.addAction(ACTION_CAPTURE_GUARD_OFF);
        context.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void showNotification() {
        OverlayNotification.show(mContext, mAppIconId);
    }

    private void hideNotification() {
        OverlayNotification.hide(mContext);
    }

    /**
     * オーバーレイの表示許可を確認します.
     *
     * @return オーバーレイの表示許可がある場合はtrue、それ以外はfalse
     */
    private boolean isOverlayAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        } else {
            return true;
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return mIsShown;
    }

    @Override
    public synchronized void enable() {
        if (mIsShown) {
            return;
        }
        if (isOverlayAllowed()) {
            showInternal();
        } else {
            startOverlayPermissionActivity();
        }
    }

    private void showInternal() {
        mMainHandler.post(() -> {
            mWindowManager.addView(mOverlayView, mLayoutParams);
            mIsShown = true;
        });
    }

    private void startOverlayPermissionActivity() {
        Intent intent = new Intent(mContext, OverlayPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mContext, intent);
    }

    @Override
    public synchronized void disable() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;
        mMainHandler.post(() -> mWindowManager.removeViewImmediate(mOverlayView));
    }

    private WindowManager.LayoutParams createLayoutParams() {
        final int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        return new WindowManager.LayoutParams(
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT
        );
    }

    private static class OverlayNotification {

        private static final int NOTIFICATION_ID = 12345;

        static PendingIntent createPendingIntent(final Context context) {
            Intent intent = new Intent(ACTION_CAPTURE_GUARD_OFF);
            return PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent, 0);
        }

        static void show(final Context context, final int appIconId) {
            PendingIntent pendingIntent = createPendingIntent(context);

            Notification notification;
            NotificationManager notificationManager = getNotificationManager(context);
            String title = context.getString(R.string.copy_guard_notification_overlay_title);
            String body = context.getString(R.string.copy_guard_notification_overlay_body);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(appIconId)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setContentIntent(pendingIntent);
                notification = notificationBuilder.build();
            } else {
                Notification.Builder notificationBuilder =
                        new Notification.Builder(context)
                                .setSmallIcon(Icon.createWithResource(context, appIconId))
                                .setContentTitle(title)
                                .setContentText(body)
                                .setContentIntent(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationBuilder.setChannelId(context.getString(R.string.copy_guard_notification_channel_id));
                }
                notification = notificationBuilder.build();
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        static void hide(final Context context) {
            NotificationManager notificationManager = getNotificationManager(context);
            notificationManager.cancel(NOTIFICATION_ID);
        }

        static NotificationManager getNotificationManager(final Context context) {
            return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }
}
