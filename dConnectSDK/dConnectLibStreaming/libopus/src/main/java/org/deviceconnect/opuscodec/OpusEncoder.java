package org.deviceconnect.opuscodec;

import android.util.Log;

public class OpusEncoder implements OpusConstants {
    private final static String TAG = "OpusEncoder";
    private final static boolean DEBUG = BuildConfig.DEBUG;

    private int mSamplingRate;
    private int mBitRate;
    private int mFrameSize;
    private int mChannels;
    private int mApplication;
    private long mNativePointer;

    private static final int MAX_PAYLOAD_BYTES = 1500;

    static {
        System.loadLibrary("opus-share");
    }

    /**
     * コンストラクタ.
     *
     * @param samplingRate サンプリングレート
     * @param channels チャンネル数
     * @param frameSize フレームサイズ
     * @param bitRate ビットレート
     * @param application アプリケーションタイプ
     * @throws NativeInterfaceException 初期化に失敗した場合に発生
     */
    public OpusEncoder(SamplingRate samplingRate, int channels, FrameSize frameSize,
                       int bitRate, Application application
    ) throws NativeInterfaceException {
        if (DEBUG) {
            Log.d(TAG, "OpusEncoder()");
            Log.d(TAG, "samplingRate:" + samplingRate);
            Log.d(TAG, "channels:" + channels);
            Log.d(TAG, "frameSize:" + frameSize);
            Log.d(TAG, "bitRate:" + bitRate);
            Log.d(TAG, "application:" + application);
        }

        if (channels != 1 && channels != 2) {
            throw new IllegalArgumentException("multi channel not support");
        }

        mSamplingRate = samplingRate.getValue();
        mChannels = channels;
        mFrameSize = samplingRate.getValue() / frameSize.getFps();
        mBitRate = bitRate;
        switch (bitRate) {
            case BITRATE_AUTO:
            case BITRATE_MAX:
                break;
            default:
                if (bitRate < BITRATE_MAX) {
                    throw new IllegalArgumentException("bitrate illegal");
                }
        }

        mApplication = application.getValue();

        mNativePointer = opusEncoderCreate(mSamplingRate, mChannels, mBitRate, mFrameSize, mApplication);
        if (mNativePointer == 0) {
            throw new NativeInterfaceException("opusEncoderCreate() failure. samplingRate: " + samplingRate +
                    " channels: " + channels + " frameSize: " + frameSize + " bitRate: " + bitRate +
                    " application: " + application);
        }
    }

    /**
     * PCM を OPUS に変換します.
     *
     * @param pcmBuffer PCM データ
     * @param pcmBufferLength PCM データサイズ
     * @param opusFrame OPUS データを格納するバッファ
     * @return OPUS データに書き込んだサイズ
     * @throws NativeInterfaceException 変換に失敗した場合に発生
     */
    public int encode(short[] pcmBuffer, int pcmBufferLength, byte[] opusFrame) throws NativeInterfaceException {
        if (mNativePointer != 0) {
            int opusFrameSize = opusEncode(mNativePointer, pcmBuffer, pcmBufferLength, opusFrame);
            if (opusFrameSize > 0) {
                return opusFrameSize;
            } else {
                release();
                throw new NativeInterfaceException("opusEncode() failure. ret=" + opusFrameSize);
            }
        } else {
            throw new IllegalStateException("encoder not created.");
        }
    }

    /**
     * OpusEncoder の後始末を行います.
     */
    public void release() {
        if (DEBUG) {
            Log.d(TAG, "stop()");
        }

        if (mNativePointer != 0) {
            opusEncoderDestroy(mNativePointer);
            mNativePointer = 0;
        }
    }

    /**
     * {@link #encode(short[], int, byte[])} の第３引数に渡すバッファを確保します.
     *
     * @return OPUS データを格納するバッファ
     */
    public byte[] bufferAllocate() {
        if (mNativePointer != 0) {
            return new byte[MAX_PAYLOAD_BYTES];
        } else {
            throw new IllegalStateException("encoder not created.");
        }
    }

    @Override
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private native long opusEncoderCreate(int samplingRate, int channels, int bitrate, int frameSize, int mode);

    private native int opusEncode(long nativePointer, short[] pcmBuffer, int pcmBufferLength, byte[] opusFrame);

    private native void opusEncoderDestroy(long nativePointer);
}
