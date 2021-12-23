package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastRTSPPreviewServer extends AbstractRTSPPreviewServer {
    ScreenCastRTSPPreviewServer(ScreenCastRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    protected VideoStream createVideoStream() {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        switch (getEncoderSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new ScreenCastH264VideoStream(recorder, 5016);
            case H265:
                return new ScreenCastH265VideoStream(recorder, 5016);
        }
    }
}
