package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.audio;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.DecoderFactory;

public class AACLATMDecoderFactory implements DecoderFactory {
    @Override
    public Decoder createDecoder() {
        return new AACLATMDecoder();
    }
}
