package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

public class CameraMJPEGEncoder extends SurfaceMJPEGEncoder {
    CameraMJPEGEncoder(Camera2Recorder recorder) {
        super(recorder.getSurfaceDrawingThread());
    }
}
