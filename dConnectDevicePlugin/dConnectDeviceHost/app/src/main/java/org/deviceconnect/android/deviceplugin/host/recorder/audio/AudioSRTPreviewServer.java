package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioSRTPreviewServer extends AbstractSRTPreviewServer {
    AudioSRTPreviewServer(final Context context, final HostMediaRecorder recorder) {
        super(context, recorder);
        setPort(recorder.getSettings().getPort(getMimeType()));
    }
}
