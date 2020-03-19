package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.util.NotificationUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastManager {

    private final Context mContext;

    private final MediaProjectionManager mMediaProjectionMgr;

    private MediaProjection mMediaProjection;

    /**
     * コールバックの通知を受けるスレッド.
     */
    private final Handler mCallbackHandler = new Handler(Looper.getMainLooper());

    /**
     * Notification Id
     */
    private static final int NOTIFICATION_ID = 3539;

    ScreenCastManager(final Context context) {
        mContext = context;
        mMediaProjectionMgr = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    /**
     * 画面が回転して解像度のスワップが必要か確認します.
     *
     * @return 解像度のスワップが必要な場合はtrue、それ以外はfalse
     */
    boolean isSwappedDimensions() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

    /**
     * 画面の回転を取得します.
     *
     * @return 画面の回転
     */
    int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        return wm.getDefaultDisplay().getRotation();
    }

    /**
     * MediaProjection の後始末を行います.
     */
    synchronized void clean() {
        MediaProjection projection = mMediaProjection;
        if (projection != null) {
            projection.stop();
            mMediaProjection = null;
        }
    }

    /**
     * MediaProjection のパーミッションの許可を要求します.
     *
     * @param callback 許可の結果を通知するコールバック
     */
    synchronized void requestPermission(final PermissionCallback callback) {
        if (mMediaProjection != null) {
            callback.onAllowed();
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
                                clean();
                            }
                        }, new Handler(Looper.getMainLooper()));
                    }
                }

                if (mMediaProjection != null) {
                    callback.onAllowed();
                } else {
                    callback.onDisallowed();
                }
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mContext.startActivity(intent);
        } else {
            // Android 10(Q) からは、バックグラウンドから Activity を起動できなくなったので、
            // Notification から起動するようにします。
            NotificationUtils.createNotificationChannel(mContext);
            NotificationUtils.notify(mContext, NOTIFICATION_ID, 0, intent,
                    mContext.getString(R.string.host_notification_projection_warnning));
        }
    }

    /**
     * Surface に端末の画面をキャストするクラスを作成します.
     *
     * @param outputSurface キャスト先の Surface
     * @param width キャスト先の Surface の横幅
     * @param height キャスト先の Surface の縦幅
     * @return SurfaceScreenCast のインスタンス
     */
    SurfaceScreenCast createScreenCast(final Surface outputSurface, int width, int height) {
        if (mMediaProjection == null) {
            throw new IllegalStateException("Media Projection is not allowed.");
        }
        return new SurfaceScreenCast(mContext, mMediaProjection, outputSurface, width, height);
    }

    /**
     * ImageReader に端末の画面をキャストするクラスを作成します.
     *
     * @param imageReader キャスト先の ImageReader
     * @param width キャスト先の ImageReader の横幅
     * @param height キャスト先の ImageReader の縦幅
     * @return ImageScreenCast のインスタンス
     */
    ImageScreenCast createScreenCast(final ImageReader imageReader, int width, int height) {
        if (mMediaProjection == null) {
            throw new IllegalStateException("Media Projection is not allowed.");
        }
        return new ImageScreenCast(mContext, mMediaProjection, imageReader, width, height);
    }

    /**
     * MediaProjection の許可確認の結果を通知するコールバック.
     */
    interface PermissionCallback {
        /**
         * MediaProjection が許可された場合に通知されます.
         */
        void onAllowed();

        /**
         * MediaProjection が許可されなかった場合に通知されます.
         */
        void onDisallowed();
    }
}
