package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;

public class UvcPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param recorder レコーダ
     */
    public UvcPreviewServerProvider(final Context context, final UvcRecorder recorder) {
        super(context, recorder);

        MediaRecorder.Settings settings = recorder.getSettings();

        addServer(new UvcMJPEGPreviewServer(context, recorder, settings.getMjpegPort(), false));
        addServer(new UvcMJPEGPreviewServer(context, recorder, settings.getMjpegSSLPort(), true));
        addServer(new UvcRTSPPreviewServer(context, recorder, settings.getRtspPort()));
        addServer(new UvcSRTPreviewServer(context, recorder, settings.getSrtPort()));
    }
}