package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param recorder レコーダ
     */
    public AudioPreviewServerProvider(Context context, HostMediaRecorder recorder) {
        super(context, recorder);

        addServer(new AudioRTSPPreviewServer(context, recorder));
        addServer(new AudioSRTPreviewServer(context, recorder));
    }
}
