package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractMJPEGPreviewServer;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

class UvcMJPEGPreviewServer extends AbstractMJPEGPreviewServer {
    UvcMJPEGPreviewServer(Context context, UvcH264Recorder recorder, int port, boolean useSSL) {
        super(context, recorder, useSSL);
        setPort(port);
    }

    @Override
    protected MJPEGEncoder createSurfaceMJPEGEncoder() {
        return new UvcMJPEGEncoder((UvcH264Recorder) getRecorder());
    }
}
