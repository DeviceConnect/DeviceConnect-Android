package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTSPPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioRTSPPreviewServer extends AbstractRTSPPreviewServer {
    AudioRTSPPreviewServer(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }
}
