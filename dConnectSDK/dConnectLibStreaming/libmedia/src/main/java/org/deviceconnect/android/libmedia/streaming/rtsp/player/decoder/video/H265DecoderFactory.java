package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.DecoderFactory;

public class H265DecoderFactory implements DecoderFactory {
    @Override
    public Decoder createDecoder() {
        return new H265Decoder();
    }
}