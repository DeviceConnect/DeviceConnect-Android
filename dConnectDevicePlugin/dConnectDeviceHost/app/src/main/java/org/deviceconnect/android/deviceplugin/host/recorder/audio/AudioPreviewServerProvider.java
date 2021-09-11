package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;

public class AudioPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param recorder レコーダ
     */
    public AudioPreviewServerProvider(Context context, HostMediaRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings) {
        switch (encoderSettings.getMimeType()) {
            case RTSP:
                return new AudioRTSPPreviewServer(getRecorder(), encoderId);
            case SRT:
                return new AudioSRTPreviewServer(getRecorder(), encoderId);
        }
        return null;
    }
}
