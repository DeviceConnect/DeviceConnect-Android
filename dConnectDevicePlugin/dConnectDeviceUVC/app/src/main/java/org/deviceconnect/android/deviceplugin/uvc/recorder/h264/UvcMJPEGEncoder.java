package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

class UvcMJPEGEncoder extends SurfaceMJPEGEncoder {

    UvcMJPEGEncoder(UvcH264Recorder recorder) {
        super(recorder.getSurfaceDrawingThread());
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
