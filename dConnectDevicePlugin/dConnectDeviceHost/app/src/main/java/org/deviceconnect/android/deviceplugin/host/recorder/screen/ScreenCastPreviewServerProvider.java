package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

/**
 * スクリーンキャストのプレビューを配信するサーバを管理するクラス.
 */
class ScreenCastPreviewServerProvider extends AbstractPreviewServerProvider {
    ScreenCastPreviewServerProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);

        HostMediaRecorder.Settings settings = recorder.getSettings();

        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, settings.getMjpegPort(), false));
        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, settings.getMjpegSSLPort(), true));
        addServer(new ScreenCastRTSPPreviewServer(context, recorder, settings.getRtspPort()));
        addServer(new ScreenCastSRTPreviewServer(context, recorder, settings.getSrtPort()));
    }
}
