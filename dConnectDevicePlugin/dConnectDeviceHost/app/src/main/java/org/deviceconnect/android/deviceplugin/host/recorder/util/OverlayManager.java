package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

/**
 * オーバーレイに表示する View の管理を行うクラス.
 */
public class OverlayManager {
    /**
     * プレビュー確認用オーバレイ用アクションを定義.
     */
    private static final String SHOW_OVERLAY_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.SHOW_OVERLAY_PREVIEW";

    /**
     * プレビュー確認用オーバレイ用アクションを定義.
     */
    private static final String HIDE_OVERLAY_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.HIDE_OVERLAY_PREVIEW";

    /**
     * カメラIDを定義.
     */
    private static final String EXTRA_CAMERA_ID = "extrea_camer_id";

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * オーバーレイのレイアウトを管理するクラス.
     */
    private final OverlayLayoutManager mOverlayLayoutManager;

    /**
     * プレビューを表示する SurfaceView.
     */
    private View mOverlayView;

    /**
     * UI スレッドで動作するハンドラ.
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * オーバーレイに表示するレコーダ.
     */
    private final HostMediaRecorder mRecorder;

    /**
     * 描画を行うクラス.
     */
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;

    /**
     * 描画先の Surface.
     */
    private Surface mSurface;

    /**
     * 描画イベントを受け取ります.
     */
    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            if (mSurface != null) {
                if (mEGLSurfaceDrawingThread.findEGLSurfaceBaseByTag(mSurface) != null) {
                    return;
                }
                mEGLSurfaceDrawingThread.addEGLSurfaceBase(mSurface);
            }
        }

        @Override
        public void onStopped() {
        }

        @Override
        public void onError(Exception e) {
        }

        @Override
        public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
            // ignore.
        }
    };

    public OverlayManager(Context context, HostMediaRecorder recorder) {
        mContext = context;
        mRecorder = recorder;
        mOverlayLayoutManager = new OverlayLayoutManager(context);
    }

    /**
     * オーバーレイの後始末を行います.
     */
    public void destroy() {
        unregisterBroadcastReceiver();
        mHandler.post(this::hideOverlay);
    }

    /**
     * 画面の回転や設定が変更されたことを受け取ります.
     */
    public void onConfigChange() {
        if (mOverlayView != null) {
            // 画面が回転したので、オーバーレイのレイアウトも調整
            mOverlayLayoutManager.update();
            mOverlayLayoutManager.updateView(mOverlayView,
                    0,
                    0,
                    mOverlayLayoutManager.getDisplayWidth(),
                    mOverlayLayoutManager.getDisplayHeight());

            if (mEGLSurfaceDrawingThread != null) {
                adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions());
            }
        }
    }

    /**
     * オーバーレイを表示します.
     */
    public void showOverlay() {
        if (mOverlayView != null) {
            return;
        }

        // Notification を閉じるイベントを送信
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if (!isOverlayAllowed()) {
            openOverlayPermissionActivity();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mOverlayView = inflater.inflate(R.layout.host_preview_surface_view, null);
        SurfaceView surfaceView = mOverlayView.findViewById(R.id.preview_surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mSurface = surfaceHolder.getSurface();
                mEGLSurfaceDrawingThread = mRecorder.getSurfaceDrawingThread();
                mEGLSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
                mEGLSurfaceDrawingThread.start();

                if (mEGLSurfaceDrawingThread != null) {
                    adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });

        mOverlayLayoutManager.addView(mOverlayView,
                0,
                0,
                mOverlayLayoutManager.getDisplayWidth(),
                mOverlayLayoutManager.getDisplayHeight(),
                "overlay-" + mRecorder.getId());
    }

    /**
     * オーバーレイを非表示にします.
     */
    public void hideOverlay() {
        if (mOverlayView == null) {
            return;
        }

        if (mEGLSurfaceDrawingThread != null) {
            mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
            mEGLSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            mEGLSurfaceDrawingThread.stop(false);
            mEGLSurfaceDrawingThread = null;
        }
        mOverlayLayoutManager.removeAllViews();
        mOverlayView = null;
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
            Size previewSize = mRecorder.getSettings().getPreviewSize();
            int cameraWidth = isSwappedDimensions ? previewSize.getHeight() : previewSize.getWidth();
            int cameraHeight = isSwappedDimensions ? previewSize.getWidth() : previewSize.getHeight();

            SurfaceView surfaceView = mOverlayView.findViewById(R.id.preview_surface_view);
            Size viewSize = new Size(mOverlayLayoutManager.getDisplayWidth(), mOverlayLayoutManager.getDisplayHeight());
            Size changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);
            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());

            mOverlayLayoutManager.updateView(mOverlayView,
                    0,
                    0,
                    changeSize.getWidth(),
                    changeSize.getHeight());

//            TextView textView = mOverlayView.findViewById(R.id.text_view);
//            textView.setVisibility(mCameraPreviewFlag ? View.VISIBLE : View.GONE);
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
    private android.util.Size calculateViewSize(int width, int height, android.util.Size viewSize) {
        int h = (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new android.util.Size(w, viewSize.getHeight());
        }
        return new android.util.Size(viewSize.getWidth(), h);
    }

    /**
     * オーバーレイの表示許可を確認します.
     *
     * @return オーバーレイの表示許可がある場合はtrue、それ以外はfalse
     */
    public boolean isOverlayAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        } else {
            return true;
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
     * 通知の ID を取得します.
     * 
     * @return 通知の ID
     */
    private int getNotificationId() {
        return 3000 + mRecorder.getId().hashCode();
    }

    /**
     * 画面にプレビューを表示するための PendingIntent を作成します.
     * @param id カメラID
     * @return PendingIntent
     */
    public PendingIntent createShowActionIntent(String id) {
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
    public PendingIntent createHideActionIntent(String id) {
        Intent intent = new Intent();
        intent.setAction(HIDE_OVERLAY_PREVIEW_ACTION);
        intent.putExtra(EXTRA_CAMERA_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }

    /**
     * 画面にプレビューを表示するためのアクションを受け取るための BroadcastReceiver を登録します.
     */
    public void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_OVERLAY_PREVIEW_ACTION);
        filter.addAction(HIDE_OVERLAY_PREVIEW_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 画面にプレビューを表示するためのアクションを受け取るための BroadcastReceiver を解除します.
     */
    public void unregisterBroadcastReceiver() {
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
            if (intent == null || mRecorder == null) {
                return;
            }

            String cameraId = intent.getStringExtra(EXTRA_CAMERA_ID);
            if (!mRecorder.getId().equals(cameraId)) {
                return;
            }

            String action = intent.getAction();
            if (SHOW_OVERLAY_PREVIEW_ACTION.equals(action)) {
                mHandler.post(() -> showOverlay());
            } else if (HIDE_OVERLAY_PREVIEW_ACTION.equals(action)) {
                mHandler.post(() -> hideOverlay());
            }
        }
    };

    /**
     * オーバーレイのパーミッションを確認します.
     *
     * @param context コンテキスト
     * @param handler ハンドラ
     * @param callback 結果を通知するコールバック
     */
    public static void checkCapability(final Context context, final Handler handler, final Callback callback) {
        final ResultReceiver cameraCapabilityCallback = new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        callback.onSuccess();
                    } else {
                        callback.onFail();
                    }
                } catch (Throwable throwable) {
                    callback.onFail();
                }
            }
        };
        final ResultReceiver overlayDrawingCapabilityCallback = new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        checkCameraCapability(context, cameraCapabilityCallback);
                    } else {
                        callback.onFail();
                    }
                } catch (Throwable throwable) {
                    callback.onFail();
                }
            }
        };
        checkOverlayDrawingCapability(context, handler, overlayDrawingCapabilityCallback);
    }

    /**
     * オーバーレイ表示のパーミッションを確認します.
     *
     * @param context コンテキスト
     * @param handler ハンドラー
     * @param resultReceiver 確認を受けるレシーバ
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static void checkOverlayDrawingCapability(final Context context, final Handler handler, final ResultReceiver resultReceiver) {
        if (Settings.canDrawOverlays(context)) {
            resultReceiver.send(Activity.RESULT_OK, null);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            IntentHandlerActivity.startActivityForResult(context, intent, new ResultReceiver(handler) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (Settings.canDrawOverlays(context)) {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    } else {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                }
            });
        }
    }

    /**
     * カメラのパーミッションを確認します.
     *
     * @param context コンテキスト
     * @param resultReceiver 確認を受けるレシーバ
     */
    private static void checkCameraCapability(final Context context, final ResultReceiver resultReceiver) {
        PermissionUtility.requestPermissions(context, new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    }

                    @Override
                    public void onFail(final String deniedPermission) {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                });
    }

    /**
     * Overlayの表示結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * 表示できたことを通知します.
         */
        void onSuccess();

        /**
         * 表示できなかったことを通知します.
         */
        void onFail();
    }
}
