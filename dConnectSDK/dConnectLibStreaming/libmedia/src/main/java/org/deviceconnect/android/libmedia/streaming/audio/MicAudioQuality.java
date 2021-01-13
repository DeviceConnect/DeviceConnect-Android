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

    public AudioPlaybackCaptureConfiguration getCaptureConfig() {
        return mCaptureConfig;
    }

    public void setCaptureConfig(AudioPlaybackCaptureConfiguration config) {
        mCaptureConfig = config;
    }
}
