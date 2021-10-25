package org.deviceconnect.android.libmedia.streaming.rtsp.session.audio;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.MediaStream;

public abstract class AudioStream extends MediaStream {
    /**
     * 音声を配信するポート番号を定義します.
     */
    private static final int AUDIO_PORT = 5004;

    public AudioStream() {
        setDestinationPort(AUDIO_PORT);
    }

    /**
     * ミュートの設定を行います.
     *
     * @param mute ミュートにする場合はtrue、それ以外はfalse
     */
    public abstract void setMute(boolean mute);

    /**
     * 音声用のエンコーダを取得します.
     *
     * @return 音声用のエンコーダ
     */
    public abstract AudioEncoder getAudioEncoder();

    @Override
    public boolean isHighPriority() {
        // Audio の場合は、スレッドを High にするので、trueを返却
        return true;
    }
}
