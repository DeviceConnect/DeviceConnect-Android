package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractBroadcastProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;

public class Camera2BroadcasterProvider extends AbstractBroadcastProvider {
    public Camera2BroadcasterProvider(Context context, Camera2Recorder recorder) {
        super(context, recorder);
    }

    @Override
    public LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings) {
        if (encoderSettings.getMimeType() == HostMediaRecorder.MimeType.RTMP) {
            return new Camera2RTMPBroadcaster((Camera2Recorder) getRecorder(), encoderId);
        }
        return null;
    }
}
