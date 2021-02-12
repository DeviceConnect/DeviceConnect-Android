package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;

class UvcPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param recorder レコーダ
     */
    UvcPreviewServerProvider(final Context context, final UvcH264Recorder recorder) {
        super(context, recorder);

        MediaRecorder.Settings settings = recorder.getSettings();

        addServer(new UvcMJPEGPreviewServer(context, recorder, settings.getMjpegPort(), false));
        addServer(new UvcMJPEGPreviewServer(context, recorder, settings.getMjpegSSLPort(), true));
        addServer(new UvcRTSPPreviewServer(context, recorder, settings.getRtspPort()));
        addServer(new UvcSRTPreviewServer(context, recorder, settings.getSrtPort()));
    }
}