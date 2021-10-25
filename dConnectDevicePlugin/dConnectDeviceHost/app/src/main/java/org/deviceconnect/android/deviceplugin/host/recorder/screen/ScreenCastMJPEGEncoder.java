package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.mjpeg.SurfaceMJPEGEncoder;

public class ScreenCastMJPEGEncoder extends SurfaceMJPEGEncoder {
    ScreenCastMJPEGEncoder(ScreenCastRecorder recorder) {
        super(recorder.getSurfaceDrawingThread());
    }
}
