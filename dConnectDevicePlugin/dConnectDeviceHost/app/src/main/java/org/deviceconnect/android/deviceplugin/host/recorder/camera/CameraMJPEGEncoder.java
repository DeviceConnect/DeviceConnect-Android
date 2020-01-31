package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

import java.io.IOException;

public class CameraMJPEGEncoder extends SurfaceMJPEGEncoder {
    /**
     * カメラ操作クラス.
     */
    private Camera2Recorder mCamera2Recorder;

    CameraMJPEGEncoder(Camera2Recorder camera2Recorder) {
        mCamera2Recorder = camera2Recorder;
    }

    // MJPEGEncoder

    @Override
    protected int getDisplayRotation() {
        return mCamera2Recorder.getDisplayRotation();
    }

    @Override
    public boolean isSwappedDimensions() {
        return mCamera2Recorder.isSwappedDimensions();
    }

    // SurfaceMJPEGEncoder

    @Override
    protected void prepare() throws IOException {
        try {
            mCamera2Recorder.startPreview(new Surface(getSurfaceTexture()));
        } catch (CameraWrapperException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void release() {
        try {
            mCamera2Recorder.stopPreview();
        } catch (CameraWrapperException e) {
            // ignore
        }
    }
}
