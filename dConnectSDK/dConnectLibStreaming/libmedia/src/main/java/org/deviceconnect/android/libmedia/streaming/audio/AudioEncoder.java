package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import org.deviceconnect.android.libmedia.streaming.MediaEncoder;

import java.io.IOException;

public abstract class AudioEncoder extends MediaEncoder {
    /**
     * ミュート設定.
     */
    private boolean mMute = true;

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
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, audioQuality.getChannel());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 0: realtime priority
            // 1: non-realtime priority (best effort).
            format.setInteger(MediaFormat.KEY_PRIORITY, 0x00);
        }

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

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    public boolean isMute() {
        return mMute;
    }

    /**
     * ミュートを設定します.
     *
     * <p>
     * デフォルトでは、ミュートは true になっています。
     * </p>
     *
     * @param mute ミュートにする場合はtrue、それ以外はfalse
     */
    public void setMute(boolean mute) {
        mMute = mute;
    }
}
