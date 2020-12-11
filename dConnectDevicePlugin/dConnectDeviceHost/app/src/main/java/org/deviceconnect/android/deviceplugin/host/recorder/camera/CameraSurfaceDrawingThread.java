package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class CameraSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * カメラ操作オブジェクト.
     */
    private final CameraWrapper mCameraWrapper;

    public CameraSurfaceDrawingThread(CameraWrapper cameraWrapper) {
        if (cameraWrapper == null) {
            throw new IllegalArgumentException("cameraWrapper is null.");
        }
        mCameraWrapper = cameraWrapper;
    }

    // EGLSurfaceDrawingThread

    @Override
    public int getDisplayRotation() {
        return mCameraWrapper.getDisplayRotation();
    }

    @Override
    public boolean isSwappedDimensions() {
        int sensorOrientation = mCameraWrapper.getSensorOrientation();
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    return true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    return true;
                }
                break;
            default:
                break;
        }
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
            mCameraWrapper.startPreview(new Surface(surfaceTexture), false);
        } catch (CameraWrapperException e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        try {
            mCameraWrapper.stopPreview();
        } catch (CameraWrapperException e) {
            e.printStackTrace();
        }
    }
}
