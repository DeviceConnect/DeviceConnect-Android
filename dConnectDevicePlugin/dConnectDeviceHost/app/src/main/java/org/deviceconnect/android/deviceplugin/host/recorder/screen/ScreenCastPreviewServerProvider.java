package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;

import javax.net.ssl.SSLContext;

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

        addServer(new ScreenCastMJPEGPreviewServer(context, false, recorder, 21000));
        addServer(new ScreenCastMJPEGPreviewServer(context, true, recorder, 21100));
        addServer(new ScreenCastRTSPPreviewServer(context, recorder, 22000));
        addServer(new ScreenCastSRTPreviewServer(context, recorder, 23000));
    }
}
