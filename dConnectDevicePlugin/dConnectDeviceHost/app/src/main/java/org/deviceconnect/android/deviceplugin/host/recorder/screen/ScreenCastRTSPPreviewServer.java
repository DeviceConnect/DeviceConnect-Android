package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastRTSPPreviewServer extends AbstractRTSPPreviewServer {
    ScreenCastRTSPPreviewServer(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);
        setPort(getStreamingSettings().getPort());
    }

    @Override
    protected VideoStream createVideoStream() {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        switch (getStreamingSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new ScreenCastH264VideoStream(recorder, 5006);
            case H265:
                return new ScreenCastH265VideoStream(recorder, 5006);
        }
    }
}
