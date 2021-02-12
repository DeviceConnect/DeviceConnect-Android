package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractMJPEGPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg.UvcMjpgRecorder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

public class UvcMJPEGPreviewServer extends AbstractMJPEGPreviewServer {
    UvcMJPEGPreviewServer(Context context, UvcRecorder recorder, int port, boolean useSSL) {
        super(context, recorder, useSSL);
        setPort(port);
    }

    @Override
    protected MJPEGEncoder createSurfaceMJPEGEncoder() {
        MediaRecorder recorder = getRecorder();
        if (recorder instanceof UvcH264Recorder) {
            return new UvcH264toMJPEGEncoder((UvcRecorder) getRecorder());
        } else if (recorder instanceof UvcMjpgRecorder) {
            return new UvcMJPEGEncoder((UvcRecorder) getRecorder());
        } else {
            return null;
        }
    }
}
