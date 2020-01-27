package org.deviceconnect.android.libsrt.server.audio;


import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libsrt.server.MediaStream;

public abstract class AudioStream extends MediaStream {
    public abstract AudioEncoder getAudioEncoder();
}
