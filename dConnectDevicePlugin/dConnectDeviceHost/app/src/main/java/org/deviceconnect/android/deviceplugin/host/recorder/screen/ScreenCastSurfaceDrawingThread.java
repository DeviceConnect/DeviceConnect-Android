package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class ScreenCastSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * Android 端末の画面をキャストを管理するクラス.
     */
    private ScreenCastRecorder mRecorder;

    /**
     * Android 端末の画面をキャストするためのクラス.
     */
    private ScreenCast mScreenCast;

    public ScreenCastSurfaceDrawingThread(ScreenCastRecorder recorder) {
        mRecorder = recorder;
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
        int w = isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
        int h = isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
        setSize(w, h);
        super.start();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScreenCast() {
        try {
            if (mScreenCast != null) {
                mScreenCast.stopCast();
                mScreenCast = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
