package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;

import java.util.List;

/**
 * スクリーンキャストのプレビューを配信するサーバを管理するクラス.
 */
class ScreenCastPreviewServerProvider extends AbstractPreviewServerProvider {
    ScreenCastPreviewServerProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings) {
        switch (encoderSettings.getMimeType()) {
            case MJPEG:
                return new ScreenCastMJPEGPreviewServer((ScreenCastRecorder) getRecorder(), encoderId);
            case RTSP:
                return new ScreenCastRTSPPreviewServer((ScreenCastRecorder) getRecorder(), encoderId);
            case SRT:
                return new ScreenCastSRTPreviewServer((ScreenCastRecorder) getRecorder(), encoderId);
        }
        return null;
    }
}
