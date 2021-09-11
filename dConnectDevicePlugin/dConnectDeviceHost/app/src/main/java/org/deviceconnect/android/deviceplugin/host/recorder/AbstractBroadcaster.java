package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public abstract class AbstractBroadcaster extends AbstractLiveStreaming implements Broadcaster {

    public AbstractBroadcaster(HostMediaRecorder recorder, String id) {
        super(recorder, id);
    }

    @Override
    public String getBroadcastURI() {
        return getEncoderSettings().getBroadcastURI();
    }

    @Override
    public String getUri() {
        return getEncoderSettings().getBroadcastURI();
    }

    /**
     * 配信するための映像用エンコーダを取得します.
     *
     * @return 配信するための映像用エンコーダ
     */
    protected VideoEncoder createVideoEncoder() {
        return null;
    }

    /**
     * 配信するための音声用エンコーダを取得します.
     *
     * @return 配信するための音声用エンコーダ
     */
    protected AudioEncoder createAudioEncoder() {
        HostMediaRecorder.Settings settings = getRecorder().getSettings();
        if (settings.isAudioEnabled()) {
            return new MicAACLATMEncoder();
        }
        return null;
    }
}
