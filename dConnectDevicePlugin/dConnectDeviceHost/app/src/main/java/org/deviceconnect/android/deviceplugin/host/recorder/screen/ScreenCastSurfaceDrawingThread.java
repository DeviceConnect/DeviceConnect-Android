package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Size;
import android.view.Surface;
import android.view.View;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.OverlayLayoutManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class ScreenCastSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * Android 端末の画面をキャストを管理するクラス.
     */
    private final ScreenCastRecorder mRecorder;

    /**
     * Android 端末の画面をキャストするためのクラス.
     */
    private ScreenCast mScreenCast;

    public ScreenCastSurfaceDrawingThread(ScreenCastRecorder recorder) {
        mRecorder = recorder;

        // 画面の更新が発生しない場合は、MediaCodec に更新イベントが発生しないので
        // ここでは、画面更新のタイムアウトを 0 にして、タイムアウトが発生しないように設定
        setTimeout(0);
    }

    // EGLSurfaceDrawingThread

    @Override
    public int getDisplayRotation() {
        // 画面は回転した場合でも回転はさせない。
        return Surface.ROTATION_0;
    }

    @Override
    public boolean isSwappedDimensions() {
        return mRecorder.getScreenCastMgr().isSwappedDimensions();
    }

    @Override
    protected void onStarted() {
        startScreenCast(getSurfaceTexture());
    }

    @Override
    protected void onStopped() {
        stopScreenCast();
    }

    @Override
    public void start() {
        HostMediaRecorder.Settings settings = mRecorder.getSettings();
        Size previewSize = settings.getPreviewSize();
        if (previewSize != null) {
            int width = isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
            int height = isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
            setSize(width, height);
            super.start();
        }
    }

    private void startScreenCast(SurfaceTexture surfaceTexture) {
        try {
            if (mScreenCast != null) {
                mScreenCast.stopCast();
            }

            HostMediaRecorder.Settings settings = mRecorder.getSettings();
            Size previewSize = settings.getPreviewSize();
            int w = isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
            int h = isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
            mScreenCast = mRecorder.getScreenCastMgr().createScreenCast(new Surface(surfaceTexture), w, h);
            mScreenCast.startCast();

            startUpdateScreenTimer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopScreenCast() {
        try {
            if (mScreenCast != null) {
                mScreenCast.stopCast();
                mScreenCast = null;
            }

            stopUpdateScreenTimer();
        } catch (Exception e) {
            // ignore.
        }
    }

    private UpdateScreenTimer mUpdateScreenTimer;

    private synchronized void startUpdateScreenTimer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                !Settings.canDrawOverlays(mRecorder.getContext())) {
            return;
        }

        if (mUpdateScreenTimer != null) {
            return;
        }
        mUpdateScreenTimer = new UpdateScreenTimer(mRecorder.getContext());
        mUpdateScreenTimer.start();
    }

    private synchronized void stopUpdateScreenTimer() {
        if (mUpdateScreenTimer != null) {
            mUpdateScreenTimer.stop();
            mUpdateScreenTimer = null;
        }
    }

    /**
     * スクリーンキャストでは、画面の更新がない場合には、MediaProjection が描画イベントを発行しません。
     * そのために MediaCodec に映像が渡すことができずに、タイムアウトを起こしてしまいます。
     *
     * その問題を回避するために、ここでは、オーバーレイを表示して、強制的に画面の更新を発生させます。
     *
     * このクラスは、強制的に画面を更新するタイミングなどを管理します。
     */
    private static class UpdateScreenTimer {
        private static final String TAG = "UpdateScreenTimer";
        private final Handler mHandler = new Handler(Looper.getMainLooper());
        private final Context mContext;
        private OverlayLayoutManager mOverlayLayoutManager;
        private int mX = 0;

        UpdateScreenTimer(Context context) {
            mContext = context;
        }

        void start() {
            mHandler.post(() -> {
                if (mOverlayLayoutManager != null) {
                    return;
                }

                View view = new View(mContext);
                view.setBackgroundColor(Color.WHITE);
                mOverlayLayoutManager = new OverlayLayoutManager(mContext);
                mOverlayLayoutManager.addView(view, 0, 0, 1, 1, TAG);
                onTick();
            });
        }

        void stop() {
            mHandler.post(() -> {
                mOverlayLayoutManager.removeAllViews();
                mOverlayLayoutManager = null;
            });
        }

        void onTick() {
            mHandler.postDelayed(() -> {
                if (mOverlayLayoutManager != null) {
                    mX++;
                    mX %= 2;
                    mOverlayLayoutManager.updateView(TAG, mX, 0, 1, 1);
                    onTick();
                }
            }, 100);
        }
    }
}
