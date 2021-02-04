package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioSRTPreviewServer extends AbstractSRTPreviewServer {
    AudioSRTPreviewServer(final Context context, final HostMediaRecorder recorder, final int port) {
        super(context, recorder);
        setPort(port);
    }
}
