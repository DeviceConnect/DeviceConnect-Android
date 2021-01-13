package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.util.NotificationUtils;

public class MediaProjectionProvider {
    /**
     * Notification Id
     */
    private static final int NOTIFICATION_ID = 3539;

    private final Context mContext;
    private final MediaProjectionManager mMediaProjectionMgr;
    private MediaProjection mMediaProjection;

    /**
     * コールバックの通知を受けるスレッド.
     */
    private final Handler mCallbackHandler = new Handler(Looper.getMainLooper());

    public MediaProjectionProvider(Context context) {
        mContext = context;
        mMediaProjectionMgr = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    /**
     * MediaProjection を取得します.
     *
     * @return MediaProjection の許可が降りていない場合は null を返却します。
     */
    public MediaProjection getMediaProjection() {
        return mMediaProjection;
    }

    /**
     * MediaProjection を停止します.
     */
    public void stop() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }

    /**
     * MediaProjection のインスタンスを要求します.
     *
     * @param callback インスタンスを通知するコールバック
     */
    public void requestPermission(final Callback callback) {
        if (mMediaProjection != null) {
            callback.onAllowed(mMediaProjection);
            return;
        }

        Intent intent = new Intent();
        intent.setClass(mContext, PermissionReceiverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PermissionReceiverActivity.EXTRA_CALLBACK, new ResultReceiver(mCallbackHandler) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    Intent data = resultData.getParcelable(PermissionReceiverActivity.RESULT_DATA);
                    if (data != null) {
                        mMediaProjection = mMediaProjectionMgr.getMediaProjection(resultCode, data);
                        mMediaProjection.registerCallback(new MediaProjection.Callback() {
                            @Override
                            public void onStop() {
                            }
                        }, new Handler(Looper.getMainLooper()));
                    }
                }

                if (mMediaProjection != null) {
                    callback.onAllowed(mMediaProjection);
                } else {
                    callback.onDisallowed();
                }
            }
        });

        // 画面に HOST プラグイン関連の画面が表示されている場合は、Activity が起動できるので
        // そのまま Context#startActivity を実行します。
        if (getApp().isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mContext.startActivity(intent);
        } else {
            // Android 10(Q) からは、バックグラウンドから Activity を起動できなくなったので、
            // Notification から起動するようにします。
            NotificationUtils.createNotificationChannel(mContext);
            NotificationUtils.notify(mContext, NOTIFICATION_ID, 0, intent,
                    mContext.getString(R.string.host_notification_projection_warnning));
        }
    }

    private HostDeviceApplication getApp() {
        return (HostDeviceApplication) mContext.getApplicationContext();
    }

    /**
     * MediaProjection 取得結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * MediaProjection の取得成功が通知されます.
         */
        void onAllowed(MediaProjection mediaProjection);

        /**
         * MediaProjection の取得失敗が通知されます.
         */
        void onDisallowed();
    }
}
