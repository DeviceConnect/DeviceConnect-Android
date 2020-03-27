package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.AudioFormat;

public abstract class AudioQuality {
    private static final int DEFAULT_SAMPLING_RATE = 8000;
    private static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_BIT_RATE = 64 * 1024;

    private String mMimeType;
    private int mSamplingRate = DEFAULT_SAMPLING_RATE;
    private int mBitRate = DEFAULT_BIT_RATE;
    private int mChannel = DEFAULT_CHANNEL;
    private int mFormat = DEFAULT_FORMAT;
    private int mMaxInputSize = 0;

    /**
     * エコーキャンセラーの使用フラグ.
     */
    private boolean mUseAEC = true;

    /**
     * コンストラクタ.
     * @param mimeType マイムタイプ
     */
    public AudioQuality(String mimeType) {
        mMimeType = mimeType;
    }

    /**
     * サポートしているサンプリングレートの一覧を取得します.
     *
     * @return サポートしているサンプリングレートの一覧
     */
    public abstract int[] getSupportSamplingRates();

    /**
     * サンプリングレートを設定します.
     * <p>
     * {@link #getSupportSamplingRates()} で取得できる値以外は設定できません。
     * </p>
     * @param samplingRate サンプリングレート
     */
    public void setSamplingRate(int samplingRate) {
        for (int s : getSupportSamplingRates()) {
            if (s == samplingRate) {
                mSamplingRate = samplingRate;
                return;
            }
        }

        throw new IllegalArgumentException("Not supported a sampling rate. samplingRate=" + samplingRate);
    }

    /**
     * チャンネルを設定します.
     *
     * <p>
     * デフォルトでは、 {@link AudioFormat#CHANNEL_IN_MONO} が設定されています。
     * </p>
     *
     * @param channel {@link AudioFormat#CHANNEL_IN_STEREO} or {@link AudioFormat#CHANNEL_IN_MONO}
     */
    public void setChannel(int channel) {
        if (channel != AudioFormat.CHANNEL_IN_STEREO && channel != AudioFormat.CHANNEL_IN_MONO) {
            throw new IllegalArgumentException("Not supported a channel. channel=" + channel);
        }
        mChannel = channel;
    }

    /**
     * ビットレートを設定します.
     *
     * <p>
     * 可変ビットレート(VBR) なので、正確にこのビットレートの値になる訳ではないので注意してください。
     * </p>
     *
     * @param bitRate ビットレート
     */
    public void setBitRate(int bitRate) {
        if (bitRate <= 0) {
            throw new IllegalArgumentException("bit rate is negative. bitRate=" + bitRate);
        }
        mBitRate = bitRate;
    }

    /**
     * 音声のフォーマットを設定します.
     *
     * <p>
     * デフォルトでは、 {@link AudioFormat#ENCODING_PCM_16BIT} が設定されています。
     * </p>
     *
     * @param format {@link AudioFormat#ENCODING_PCM_16BIT} or {@link AudioFormat#ENCODING_PCM_8BIT}
     */
    public void setFormat(int format) {
        if (format != AudioFormat.ENCODING_PCM_16BIT && format != AudioFormat.ENCODING_PCM_8BIT) {
            throw new IllegalArgumentException("Not supported a format. format=" + format);
        }
        mFormat = format;
    }

    /**
     * エコーキャンセラーの使用状態を確認します.
     *
     * @return 使用する場合はtrue、それ以外はfalse
     */
    public boolean isUseAEC() {
        return mUseAEC;
    }

    /**
     * エコーキャンセラーの設定を行います.
     *
     * @param useAEC 使用する場合はtrue、それ以外はfalse
     */
    public void setUseAEC(boolean useAEC) {
        mUseAEC = useAEC;
    }

    /**
     * 音声のマイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    public String getMimeType() {
        return mMimeType;
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
     * 音声のチャンネルを取得します.
     *
     * @return 音声のチャンネル
     */
    public int getChannel() {
        return mChannel;
    }

    /**
     * 音声のフォーマットを取得します.
     *
     * @return 音声のフォーマット
     */
    public int getFormat() {
        return mFormat;
    }

    /**
     * ビットレートを取得します.
     *
     * @return ビットレート
     */
    public int getBitRate() {
        return mBitRate;
    }

    /**
     * チャンネル数を取得します.
     *
     * @return チャンネル数
     */
    public int getChannelCount() {
        return mChannel == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1;
    }

    /**
     * MediaCodec の InputBuffer の最大値を取得します.
     *
     * @return MediaCodec の InputBuffer の最大値
     */
    public int getMaxInputSize() {
        return mMaxInputSize;
    }

    /**
     * MediaCodec の InputBuffer の最大値を設定します.
     *
     * @param maxInputSize MediaCodec の InputBuffer の最大値
     */
    public void setMaxInputSize(int maxInputSize) {
        mMaxInputSize = maxInputSize;
    }

    @Override
    public String toString() {
        return "AudioQuality{" +
                "mMimeType='" + mMimeType + '\'' +
                ", mSamplingRate=" + mSamplingRate +
                ", mBitRate=" + mBitRate +
                ", mChannel=" + mChannel +
                ", mFormat=" + mFormat +
                '}';
    }
}
