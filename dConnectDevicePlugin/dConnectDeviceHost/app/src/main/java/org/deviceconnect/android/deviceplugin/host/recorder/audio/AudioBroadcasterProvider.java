package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;

public class AudioBroadcasterProvider extends AbstractBroadcastProvider {

    public AudioBroadcasterProvider(Context context, HostAudioRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings) {
        if (encoderSettings.getMimeType() == HostMediaRecorder.MimeType.RTMP) {
            return new AudioRTMPBroadcaster((HostAudioRecorder) getRecorder(), encoderId);
        }
        return null;
    }
}
