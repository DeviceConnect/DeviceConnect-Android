package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.player.UVCPlayer;
import org.deviceconnect.android.libuvc.player.UVCPlayerException;

public class UvcSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * レコーダ.
     */
    private final UvcRecorder mRecorder;

    /**
     * UVC からの映像をデコードするためのプレイヤー.
     */
    private UVCPlayer mPlayer;

    public UvcSurfaceDrawingThread(UvcRecorder recorder) {
        if (recorder == null) {
            throw new IllegalArgumentException("recorder is null.");
        }
        mRecorder = recorder;
    }

    public UvcRecorder getRecorder() {
        return mRecorder;
    }

    // EGLSurfaceDrawingThread

    @Override
    public void start() {
        MediaRecorder.Settings settings = mRecorder.getSettings();
        Size previewSize = settings.getPreviewSize();
        setSize(previewSize.getWidth(), previewSize.getHeight());
        super.start();
    }

    @Override
    public int getDisplayRotation() {
        return Surface.ROTATION_0;
    }

    @Override
    public boolean isSwappedDimensions() {
        return false;
    }

    @Override
    protected void onStarted() {
        startCamera(getSurfaceTexture());
    }

    @Override
    protected void onStopped() {
        stopCamera();
    }

    private synchronized void startCamera(SurfaceTexture surfaceTexture) {
        try {
            UvcRecorder.UvcSettings settings = (UvcRecorder.UvcSettings) mRecorder.getSettings();

            UVCCamera camera = mRecorder.getUVCCamera();
            if (camera.isRunning()) {
                throw new RuntimeException("UVC camera is already running.");
            }

            Parameter parameter = settings.getParameter();
            if (parameter == null) {
                throw new RuntimeException("UVC parameter not found.");
            }

            mPlayer = new UVCPlayer();
            mPlayer.setSurface(new Surface(surfaceTexture));
            mPlayer.setOnEventListener(new UVCPlayer.OnEventListener() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onStopped() {
                }

                @Override
                public void onError(UVCPlayerException e) {
                }
            });
            mPlayer.start(camera, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void stopCamera() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (Exception e) {
                // ignore.
            }
            mPlayer = null;
        }
    }
}
