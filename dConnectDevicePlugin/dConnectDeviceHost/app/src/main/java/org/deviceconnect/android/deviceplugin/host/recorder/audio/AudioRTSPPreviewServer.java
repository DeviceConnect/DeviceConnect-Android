package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioRTSPPreviewServer extends AbstractRTSPPreviewServer {
    AudioRTSPPreviewServer(Context context, HostMediaRecorder recorder, int port) {
        super(context, recorder);
        setPort(port);
    }
}
