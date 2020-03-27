package org.deviceconnect.opuscodec;

public interface OpusConstants {
    /**
     * サンプリング周波数(8000Hz - 48000Hz).
     */
    enum SamplingRate {
        E_8K(8000),
        E_12K(12000),
        E_16K(16000),
        E_24K(24000),
        E_48K(48000);

        private int mValue;

        SamplingRate(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static SamplingRate samplingRateOf(int samplingRate) {
            for (SamplingRate rate : values()) {
                if (samplingRate == rate.mValue) {
                    return rate;
                }
            }
            return null;
        }
    }

    /**
     * フレームサイズ(2.5ms - 60ms).
     */
    enum FrameSize {
        E_2_5_MS(400),
        E_5_MS(200),
        E_10_MS(100),
        E_20_MS(50),
        E_40_MS(25);

        private int mFps;

        FrameSize(int fps) {
            mFps = fps;
        }

        public int getFps() {
            return mFps;
        }

        public static FrameSize frameSizeOf(int fps) {
            for (FrameSize frameSize : values()) {
                if (fps == frameSize.mFps) {
                    return frameSize;
                }
            }
            return null;
        }
    }

    /**
     * アプリケーション.
     */
    enum Application {
        E_VOIP(2048),
        E_AUDIO(2049),
        E_REST_LD(2051);

        private int mValue;

        Application(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static Application applicationOf(int app) {
            for (Application a : values()) {
                if (a.mValue == app) {
                    return a;
                }
            }
            return null;
        }
    }

    /**
     * ビットレートを自動で設定します.
     */
    int BITRATE_AUTO  = 0;

    /**
     * ビットレートを最大値で設定します.
     */
    int BITRATE_MAX = -1;
}
