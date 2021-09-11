package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;

public class ScreenCastBroadcasterProvider extends AbstractBroadcastProvider {

    public ScreenCastBroadcasterProvider(Context context, ScreenCastRecorder recorder) {
        super(context, recorder);
    }

    @Override
    public LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings) {
        if (encoderSettings.getMimeType() == HostMediaRecorder.MimeType.RTMP) {
            return new ScreenCastRTMPBroadcaster((ScreenCastRecorder) getRecorder(), encoderId);
        }
        return null;
    }
}
