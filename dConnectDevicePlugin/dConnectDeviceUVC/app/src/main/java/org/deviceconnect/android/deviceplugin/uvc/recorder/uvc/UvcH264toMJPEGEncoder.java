package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

public class UvcH264toMJPEGEncoder extends SurfaceMJPEGEncoder {

    public UvcH264toMJPEGEncoder(UvcRecorder recorder) {
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
