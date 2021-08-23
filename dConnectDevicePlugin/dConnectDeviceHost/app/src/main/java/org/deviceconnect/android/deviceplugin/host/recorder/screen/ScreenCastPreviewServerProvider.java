package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;

/**
 * スクリーンキャストのプレビューを配信するサーバを管理するクラス.
 */
class ScreenCastPreviewServerProvider extends AbstractPreviewServerProvider {
    ScreenCastPreviewServerProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);

        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, false));
        addServer(new ScreenCastMJPEGPreviewServer(context, recorder, true));
        addServer(new ScreenCastRTSPPreviewServer(context, recorder));
        addServer(new ScreenCastSRTPreviewServer(context, recorder));
    }
}
