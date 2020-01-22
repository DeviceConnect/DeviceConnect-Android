package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;

public abstract class AudioDecoder implements Decoder {
    /**
     * 音声を再生するための AudioTrack.
     */
    private AudioTrack mAudioTrack;

    /**
     * 一時的に音声のデータを格納するバッファ.
     */
    private byte[] mAudioOutTempBuf = new byte[4096];

    /**
     * サンプリングレート.
     */
    private int mSamplingRate = 44100;

    /**
     * チャンネル数.
     */
    private int mChannelCount = 1;

    /**
     * ミュート設定.
     */
    private boolean mMute;

    /**
     * ミュート設定を取得します.
     *
     * @return ミュート設定
     */
    public boolean isMute() {
        return mMute;
    }

    /**
     * ミュート設定を行います.
     *
     * @param mute ミュート
     */
    public void setMute(boolean mute) {
        mMute = mute;
    }

    /**
     * サンプリングレートを取得します.
     *
     * @return サンプリングレート
     */
    public int getSamplingRate() {
        return mSamplingRate;
    }

    /**
     * サンプリングレートを設定します.
     *
     * @param samplingRate サンプリングレート
     */
    public void setSamplingRate(int samplingRate) {
        mSamplingRate = samplingRate;
    }

    /**
     * チャンネル数を取得します.
     *
     * @return チャンネル数
     */
    public int getChannelCount() {
        return mChannelCount;
    }

    /**
     * チャンネル数を設定します
     *
     * @param channelCount チャンネル数
     */
    public void setChannelCount(int channelCount) {
        mChannelCount = channelCount;
    }

    /**
     * 指定されたサンプリングレートとチャンネル数で AudioTrack を作成します.
     */
    void createAudioTrack() {
        int bufSize = AudioTrack.getMinBufferSize(mSamplingRate,
                mChannelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT) * 2;

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSamplingRate,
                mChannelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize,
                AudioTrack.MODE_STREAM);

        mAudioTrack.play();
    }

    /**
     * AudioTrack を破棄します.
     */
    void releaseAudioTrack() {
        if (mAudioTrack != null) {
            try {
                mAudioTrack.stop();
            } catch (IllegalStateException e) {
                // ignore.
            }

            try {
                mAudioTrack.release();
            } catch (Exception e) {
                // ignore.
            }

            mAudioTrack = null;
        }
    }

    /**
     * AudioTrack に音声を書き込みます.
     *
     * @param buffer 書き込むデータ
     * @param offset オフセット
     * @param size データサイズ
     * @param presentationTimeUs プレゼンテーションタイム
     */
    void writeAudioData(ByteBuffer buffer, int offset, int size, long presentationTimeUs) {
        if (mMute) {
            return;
        }

        if (mAudioOutTempBuf.length < size) {
            mAudioOutTempBuf = new byte[size];
        }
        buffer.position(offset);
        buffer.get(mAudioOutTempBuf, 0, size);
        buffer.clear();

        if (mAudioTrack != null) {
            mAudioTrack.write(mAudioOutTempBuf, 0, size);
        }
    }
}
