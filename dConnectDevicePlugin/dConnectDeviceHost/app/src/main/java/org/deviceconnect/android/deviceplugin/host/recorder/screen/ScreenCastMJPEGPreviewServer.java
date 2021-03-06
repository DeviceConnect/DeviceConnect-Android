package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMJPEGPreviewServer;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastMJPEGPreviewServer extends AbstractMJPEGPreviewServer {

    ScreenCastMJPEGPreviewServer(Context context, ScreenCastRecorder recorder, int port, boolean useSSL) {
        super(context, recorder, useSSL);
        setPort(port);
    }

    @Override
    protected MJPEGEncoder createSurfaceMJPEGEncoder() {
        return new ScreenCastMJPEGEncoder((ScreenCastRecorder) getRecorder());
    }
}
