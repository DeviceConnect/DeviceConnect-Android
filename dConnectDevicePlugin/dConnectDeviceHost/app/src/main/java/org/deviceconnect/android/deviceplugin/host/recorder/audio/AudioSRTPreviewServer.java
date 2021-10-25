package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioSRTPreviewServer extends AbstractSRTPreviewServer {
    AudioSRTPreviewServer(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }
}
