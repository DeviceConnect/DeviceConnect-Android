package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;

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

        ScreenCastMJPEGPreviewServer mjpegServer = new ScreenCastMJPEGPreviewServer(context, recorder);
        mjpegServer.setQuality(RecorderSettingData.getInstance(getContext()).readPreviewQuality(recorder.getId()));
        addServer(mjpegServer);
        addServer(new ScreenCastRTSPPreviewServer(context, recorder));
        addServer(new ScreenCastSRTPreviewServer(context, recorder));
    }
}
