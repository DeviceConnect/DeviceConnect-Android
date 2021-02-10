package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.AudioPlaybackCaptureConfiguration;

public class MicAudioQuality extends AudioQuality {

    /**
     * AAC で使用できるサンプリングレートを定義します.
     */
    private static final int[] SUPPORT_AUDIO_SAMPLING_RATES = {
            96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000,  // 11
            7350,  // 12
            -1,   // 13
            -1,   // 14
            -1,   // 15
    };

    /**
     * 音声入力ソースの定義.
     */
    public enum Source {
        DEFAULT,
        MIC,
        APP
    }

    /**
     * 音声入力ソース.
     */
    private Source mSource = Source.DEFAULT;

    /**
     * アプリの音声録音設定.
     */
    private AudioPlaybackCaptureConfiguration mCaptureConfig;

    /**
     * コンストラクタ.
     *
     * @param mimeType マイムタイプ
     */
    public MicAudioQuality(String mimeType) {
        super(mimeType);
    }

    @Override
    public int[] getSupportSamplingRates() {
        return SUPPORT_AUDIO_SAMPLING_RATES;
    }

    /**
     * 音声入力ソースを取得します.
     *
     * @return 音声入力ソース
     */
    public Source getSource() {
        return mSource;
    }

    /**
     * 音声入力ソースを設定します.
     *
     * @param source 音声入力ソース
     */
    public void setSource(Source source) {
        mSource = source;
    }

    /**
     * アプリ音声録音設定を取得します.
     *
     * @return アプリ音声録音設定
     */
    public AudioPlaybackCaptureConfiguration getCaptureConfig() {
        return mCaptureConfig;
    }

    /**
     * アプリ音声録音設定を設定します.
     *
     * @param config アプリ音声録音設定を取得します.
     */
    public void setCaptureConfig(AudioPlaybackCaptureConfiguration config) {
        mCaptureConfig = config;
    }
}
