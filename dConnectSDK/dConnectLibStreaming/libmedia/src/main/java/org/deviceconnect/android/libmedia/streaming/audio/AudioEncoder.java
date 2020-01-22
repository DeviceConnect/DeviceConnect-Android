package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;

import org.deviceconnect.android.libmedia.streaming.MediaEncoder;

public abstract class AudioEncoder extends MediaEncoder {
    /**
     * 音声のエンコード設定を取得します.
     *
     * @return 音声のエンコード設定
     */
    public abstract AudioQuality getAudioQuality();

    @Override
    protected void prepare() throws IOException {
        AudioQuality audioQuality = getAudioQuality();

        MediaFormat format = MediaFormat.createAudioFormat(audioQuality.getMimeType(),
                audioQuality.getSamplingRate(), audioQuality.getChannelCount());
        format.setString(MediaFormat.KEY_MIME, audioQuality.getMimeType());
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioQuality.getSamplingRate());
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioQuality.getBitRate());
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioQuality.getChannelCount());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mMediaCodec = MediaCodec.createEncoderByType(audioQuality.getMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
}
