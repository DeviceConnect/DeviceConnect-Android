package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

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

    private Context mContext;

    /**
     * オーバーレイのレイアウトを管理するクラス.
     */
    private OverlayLayoutManager mOverlayLayoutManager;

    /**
     * プレビューを表示する SurfaceView.
     */
    private View mOverlayView;

    /**
     * UI スレッドで動作するハンドラ.
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private HostMediaRecorder mRecorder;
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private Surface mSurface;

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

    public void destroy() {
        unregisterBroadcastReceiver();
        mHandler.post(this::hideOverlay);
    }

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

    public void showOverlay() {
        if (mOverlayView != null) {
            return;
        }

        // Notification を閉じるイベントを送信
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if (!mOverlayLayoutManager.isOverlayAllowed()) {
            openOverlayPermissionActivity();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mOverlayView = inflater.inflate(R.layout.host_preview_overlay, null);
        SurfaceView surfaceView = mOverlayView.findViewById(R.id.fragment_host_preview_surface_view);
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

            SurfaceView surfaceView = mOverlayView.findViewById(R.id.fragment_host_preview_surface_view);
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
     * オーバーレイの許可を求めるための Activity を開きます.
     */
    private void openOverlayPermissionActivity() {
        Intent intent = new Intent();
        intent.setClass(mContext, OverlayPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private int getNotificationId() {
        return 1111;
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
}
