package org.deviceconnect.opuscodec;

import android.util.Log;

public class OpusDecoder implements OpusConstants {
    private final static String TAG = "OpusDecoder";
    private final static boolean DEBUG = BuildConfig.DEBUG;

    private int mSamplingRate;
    private int mChannels;
    private int mFrameSize;
    private long mNativePointer;

    static {
        System.loadLibrary("opus-share");
    }

    /**
     * コンストラクタ.
     *
     * @param samplingRate サンプリングレート
     * @param channels チャンネル数
     * @param frameSize フレームサイズ
     * @throws NativeInterfaceException 初期化に失敗した場合に発生
     */
    public OpusDecoder(SamplingRate samplingRate, int channels, FrameSize frameSize) throws NativeInterfaceException {
        if (DEBUG) {
            Log.d(TAG, "OpusDecorder");
            Log.d(TAG, "samplingRate:" + samplingRate);
            Log.d(TAG, "channels:" + channels);
            Log.d(TAG, "frameSize:" + frameSize);
        }

        if (channels != 1 && channels != 2) {
            throw new IllegalArgumentException("multi channel not support");
        }

        mSamplingRate = samplingRate.getValue();
        mChannels = channels;
        mFrameSize = samplingRate.getValue() / frameSize.getFps();

        mNativePointer = opusDecoderCreate(mSamplingRate, mChannels);
        if (mNativePointer == 0) {
            throw new NativeInterfaceException("opusDecoderCreate() failure. samplingRate: " + samplingRate
                    + " channels: " + channels + " frameSize: " + frameSize);
        }
    }

    /**
     * OPUS のデータを PCM に変換します.
     *
     * @param opusFrame OPUS のデータ
     * @param opusFrameSize OPUS のデータサイズ
     * @param pcmBuffer PCM のデータを格納するバッファ
     * @return 変換された PCM のデータサイズ
     * @throws NativeInterfaceException 変換に失敗した場合に発生
     */
    public int decode(byte[] opusFrame, int opusFrameSize, short[] pcmBuffer) throws NativeInterfaceException {
        if (DEBUG) {
            Log.d(TAG, "decode");
            Log.d(TAG, "opusFrameSize : " + opusFrameSize);
        }

        if (mNativePointer != 0) {
            int pcmBufferLength = opusDecode(mNativePointer, opusFrame, opusFrameSize, pcmBuffer, pcmBuffer.length);
            if (pcmBufferLength > 0) {
                return pcmBufferLength;
            } else {
                release();
                throw new NativeInterfaceException("opusDecode() failure. ret=" + pcmBufferLength);
            }
        } else {
            throw new IllegalStateException("decoder not created.");
        }
    }

    /**
     * 後始末を行います.
     */
    public void release() {
        if (DEBUG) {
            Log.d(TAG, "stop()");
        }

        if (mNativePointer != 0) {
            opusDecoderDestroy(mNativePointer);
            mNativePointer = 0;
        }
    }

    /**
     * {@link #decode(byte[], int, short[])} の第３引数に渡すバッファを取得します.
     *
     * @return PCM を格納するバッファ
     */
    public short[] bufferAllocate() {
        if (DEBUG) {
            Log.d(TAG, "bufferAllocate()");
        }
        if (mNativePointer != 0) {
            return new short[calcBufferSize()];
        } else {
            throw new IllegalStateException("decoder not created");
        }
    }

    /**
     * バッファサイズを計算します.
     *
     * @return バッファサイズ
     */
    private int calcBufferSize() {
        return mFrameSize * mChannels * 2;
    }

    @Override
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private native long opusDecoderCreate(int samplingRate, int channels);

    private native int opusDecode(long nativePointer, byte[] opusFrame, int opusFrameSize, short[] pcmBuffer, int pcmBufferSize);

    private native void opusDecoderDestroy(long nativePointer);
}
