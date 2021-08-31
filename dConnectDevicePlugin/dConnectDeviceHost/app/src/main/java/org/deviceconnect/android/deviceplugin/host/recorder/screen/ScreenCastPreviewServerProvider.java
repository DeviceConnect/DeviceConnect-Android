package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.util.List;

/**
 * スクリーンキャストのプレビューを配信するサーバを管理するクラス.
 */
class ScreenCastPreviewServerProvider extends AbstractPreviewServerProvider {
    ScreenCastPreviewServerProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);

        List<String> previewList = recorder.getSettings().getPreviewServerList();
        for (String name : previewList) {
            HostMediaRecorder.StreamingSettings s = recorder.getSettings().getPreviewServer(name);
            if (s != null) {
                String mimeType = s.getMimeType();
                if ("video/x-mjpeg".equalsIgnoreCase(mimeType)) {
                    addServer(new ScreenCastMJPEGPreviewServer(context, recorder, false));
                } else if ("video/x-rtp".equalsIgnoreCase(mimeType)) {
                    addServer(new ScreenCastRTSPPreviewServer(context, recorder));
                } else if ("video/MP2T".equalsIgnoreCase(mimeType)) {
                    addServer(new ScreenCastSRTPreviewServer(context, recorder));
                }
            }
        }
    }
}
