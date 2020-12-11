package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

public class CameraMJPEGEncoder extends SurfaceMJPEGEncoder {

    CameraMJPEGEncoder(Camera2Recorder camera2Recorder) {
        super(camera2Recorder.getSurfaceDrawingThread());
    }

    // SurfaceMJPEGEncoder

    @Override
    protected void prepare() {
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {

    }

    @Override
    protected void release() {
    }
}
