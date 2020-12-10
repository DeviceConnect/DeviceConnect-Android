package org.deviceconnect.android.libmedia.streaming.mjpeg;

import org.deviceconnect.android.libmedia.streaming.util.CameraSurfaceDrawingThread;

public class CameraMJPEGEncoder extends SurfaceMJPEGEncoder {
    public CameraMJPEGEncoder(CameraSurfaceDrawingThread thread) {
        super(thread);
    }

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
