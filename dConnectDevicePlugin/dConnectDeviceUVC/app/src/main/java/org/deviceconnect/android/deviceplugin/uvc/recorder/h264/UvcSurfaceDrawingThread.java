package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.player.UVCPlayer;
import org.deviceconnect.android.libuvc.player.UVCPlayerException;

class UvcSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * レコーダ.
     */
    private final UvcH264Recorder mRecorder;

    /**
     * UVC からの映像をデコードするためのプレイヤー.
     */
    private UVCPlayer mPlayer;

    public UvcSurfaceDrawingThread(UvcH264Recorder recorder) {
        if (recorder == null) {
            throw new IllegalArgumentException("recorder is null.");
        }
        mRecorder = recorder;
    }

    public UvcH264Recorder getRecorder() {
        return mRecorder;
    }

    // EGLSurfaceDrawingThread

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

    private void startCamera(SurfaceTexture surfaceTexture) {
        try {
            UvcH264Recorder.UvcSettings settings = (UvcH264Recorder.UvcSettings) mRecorder.getSettings();

            UVCCamera camera = mRecorder.getUVCCamera();
            if (camera.isRunning()) {
                throw new RuntimeException("UVC camera is already running.");
            }

            Parameter parameter = settings.getParameter();
            if (parameter == null) {
                throw new RuntimeException();
            }
            parameter.setUseH264(true);

            mPlayer = new UVCPlayer();
            mPlayer.setSurface(new Surface(surfaceTexture));
            mPlayer.setOnEventListener(new UVCPlayer.OnEventListener() {
                @Override
                public void onStarted() {
                    Log.i("ABC", "UVCPlayer::onStarted");
                }

                @Override
                public void onStopped() {
                    Log.i("ABC", "UVCPlayer::onStopped");
                }

                @Override
                public void onError(UVCPlayerException e) {
                    Log.e("ABC", "UVCPlayer::onError", e);
                }
            });
            mPlayer.start(camera, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopCamera() {
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
