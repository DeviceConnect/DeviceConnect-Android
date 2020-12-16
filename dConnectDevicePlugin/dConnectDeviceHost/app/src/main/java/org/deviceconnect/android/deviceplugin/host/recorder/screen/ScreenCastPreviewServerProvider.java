package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

/**
 * スクリーンキャストのプレビューを配信するサーバを管理するクラス.
 */
class ScreenCastPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * Notification の識別子を定義.
     */
    private static final int NOTIFICATION_ID = 2001;

    ScreenCastPreviewServerProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder, NOTIFICATION_ID);

        HostMediaRecorder.Settings settings = recorder.getSettings();

        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, false, settings.getMjpegPort()));
        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, true, settings.getMjpegSSLPort()));
        addServer(new ScreenCastRTSPPreviewServer(context, recorder, settings.getRtspPort()));
        addServer(new ScreenCastSRTPreviewServer(context, recorder, settings.getSrtPort()));
    }
}
