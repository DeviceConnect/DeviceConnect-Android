package org.deviceconnect.opuscodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.util.Log;

public class MicOpusRecorder {
    private final static boolean DEBUG = BuildConfig.DEBUG;
    private final static String TAG = "MicAudioRecord";

    private final OpusEncoder.SamplingRate mSamplingRate;
    private final OpusEncoder.FrameSize mFrameSize;
    private final OpusEncoder.Application mApplication;
    private int mBitRate;
    private int mChannels;
    private boolean mUseAEC;
    private boolean mMute;
    private AudioRecordCallback mAudioRecordCallback;
    private MicRecordThread mMicRecordThread;

    /**
     * 音声を録音したデータを通知するコールバック.
     */
    public interface AudioRecordCallback {
        /**
         * 音声を録音したデータを通知します.
         *
         * <p>
         * opusFrameBuffer は内部で使い回されるので、外部で使用する場合にはコピーしてください。
         * </p>
         *
         * @param opusFrameBuffer 音声データ
         * @param opusFrameBufferLength 音声データサイズ
         */
        void onPeriodicNotification(byte[] opusFrameBuffer, int opusFrameBufferLength);

        /**
         * エンコーダでエラーが発生したことを通知します.
         */
        void onEncoderError();
    }

    public MicOpusRecorder(
            final OpusEncoder.SamplingRate samplingRate,
            final int channels,
            final OpusEncoder.FrameSize frameSize,
            final OpusEncoder.Application application,
            final AudioRecordCallback callback) {
        this(samplingRate, channels, frameSize, OpusConstants.BITRATE_AUTO, application, callback);
    }

    /**
     * コンストラクタ.
     * @param samplingRate サンプリングレート
     * @param channels チャンネル数
     * @param frameSize フレームサイズ
     * @param bitRate ビットレート
     * @param application アプリケーションタイプ
     * @param callback コールバック
     */
    public MicOpusRecorder(final OpusEncoder.SamplingRate samplingRate,
                           final int channels,
                           final OpusEncoder.FrameSize frameSize,
                           final int bitRate,
                           final OpusEncoder.Application application,
                           final AudioRecordCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "MicOpusRecorder()");
            Log.d(TAG, "  samplingRate : " + samplingRate);
            Log.d(TAG, "  frameSize : " + frameSize);
            Log.d(TAG, "  bitRate : " + bitRate);
            Log.d(TAG, "  application : " + application);
        }

        if (samplingRate == null) {
            throw new IllegalArgumentException("samplingRate is null.");
        }

        if (frameSize == null) {
            throw new IllegalArgumentException("frameSize is null.");
        }

        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }

        if (channels != 1 && channels != 2) {
            throw new IllegalArgumentException("channels is invalid.");
        }

        switch (bitRate) {
            case OpusConstants.BITRATE_AUTO:
            case OpusConstants.BITRATE_MAX:
                break;
            default:
                if (bitRate < OpusConstants.BITRATE_MAX) {
                    throw new IllegalArgumentException("bitrate illegal.");
                }
        }

        mSamplingRate = samplingRate;
        mChannels = channels;
        mFrameSize = frameSize;
        mBitRate = bitRate;
        mApplication = application;
        mAudioRecordCallback = callback;
    }

    /**
     * 録音を開始します.
     */
    public synchronized void start() {
        if (DEBUG) {
            Log.d(TAG, "MicOpusRecorder::start()");
        }

        if (mMicRecordThread != null) {
            return;
        }
        mMicRecordThread = new MicRecordThread();
        mMicRecordThread.setName("opus-mic");
        mMicRecordThread.start();
    }

    /**
     * 録音を停止します.
     */
    public synchronized void stop() {
        if (DEBUG) {
            Log.d(TAG, "MicOpusRecorder::stop()");
        }

        if (mMicRecordThread != null) {
            mMicRecordThread.terminate();
            mMicRecordThread = null;
        }
    }

    /**
     * エコーキャンセラーの使用を設定します.
     *
     * @param useAEC エコーキャンセラーを使用する場合はtrue、それ以外はfalse
     */
    public void setUseAEC(boolean useAEC) {
        if (mMicRecordThread != null) {
            throw new IllegalStateException("");
        }
        mUseAEC = useAEC;
    }

    /**
     * エコーキャンセラーの使用状態を取得します.
     *
     * @return エコーキャンセラーを使用する場合はtrue、それ以外はfalse
     */
    public boolean isUseAEC() {
        if (AcousticEchoCanceler.isAvailable()) {
            return mUseAEC;
        }
        return false;
    }

    public boolean isMute() {
        return mMute;
    }

    public void setMute(boolean mute) {
        mMute = mute;
    }

    /**
     * 音声レコード用のスレッド.
     */
    private class MicRecordThread extends Thread {
        /**
         * マイクから音声をレコードするクラス.
         */
        private AudioRecord mAudioRecord;

        /**
         * エコーキャンセラー.
         */
        private AcousticEchoCanceler mEchoCanceler;

        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * 音声レコード用のスレッドを停止します.
         */
        private void terminate() {
            mStopFlag = true;

            if (mAudioRecord != null) {
                try {
                    mAudioRecord.stop();
                } catch (Exception e) {
                    // ignore.
                }
                mAudioRecord.release();
                mAudioRecord = null;
            }

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * 音声をレコードして、MediaCodec に渡します.
         */
        private void recordAudio() throws NativeInterfaceException {
            int samplingRate = mSamplingRate.getValue();
            int channels = mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channels, audioFormat) * 4;
            int oneFrameDataCount = mSamplingRate.getValue() / mFrameSize.getFps();

            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    samplingRate,
                    channels,
                    audioFormat,
                    bufferSize);

            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                if (mAudioRecordCallback != null) {
                    mAudioRecordCallback.onEncoderError();
                }
                return;
            }

            if (mUseAEC && AcousticEchoCanceler.isAvailable()) {
                // ノイズキャンセラー
                mEchoCanceler = AcousticEchoCanceler.create(mAudioRecord.getAudioSessionId());
                if (mEchoCanceler != null) {
                    int ret = mEchoCanceler.setEnabled(true);
                    if (ret != AudioEffect.SUCCESS) {
                        if (DEBUG) {
                            Log.w(TAG, "AcousticEchoCanceler is not supported.");
                        }
                    }
                }
            }

            OpusEncoder opusEncoder = null;

            try {
                opusEncoder = new OpusEncoder(mSamplingRate, mChannels, mFrameSize, mBitRate, mApplication);

                mAudioRecord.startRecording();

                short[] emptyBuffer = new short[oneFrameDataCount];
                short[] pcmBuffer = new short[oneFrameDataCount];
                byte[] opusFrameBuffer = opusEncoder.bufferAllocate();
                while (!mStopFlag) {
                    int readSize = mAudioRecord.read(pcmBuffer, 0, oneFrameDataCount);
                    if (readSize > 0) {
                        int opusFrameBufferLength;
                        if (isMute()) {
                            opusFrameBufferLength = opusEncoder.encode(emptyBuffer, readSize, opusFrameBuffer);
                        } else {
                            opusFrameBufferLength = opusEncoder.encode(pcmBuffer, readSize, opusFrameBuffer);
                        }

                        if (opusFrameBufferLength > 0 && mAudioRecordCallback != null) {
                            mAudioRecordCallback.onPeriodicNotification(opusFrameBuffer, opusFrameBufferLength);
                        }
                    } else if (readSize == AudioRecord.ERROR_INVALID_OPERATION) {
                        if (DEBUG) {
                            Log.e(TAG, "Invalid operation error.");
                        }
                        break;
                    } else if (readSize == AudioRecord.ERROR_BAD_VALUE) {
                        if (DEBUG) {
                            Log.e(TAG, "Bad value error.");
                        }
                        break;
                    } else if (readSize == AudioRecord.ERROR) {
                        if (DEBUG) {
                            Log.e(TAG, "Unknown error.");
                        }
                        break;
                    }
                }
            } finally {
                if (mEchoCanceler != null) {
                    mEchoCanceler.release();
                    mEchoCanceler = null;
                }

                if (opusEncoder != null) {
                    opusEncoder.release();
                }
            }
        }

        @Override
        public void run() {
            try {
                recordAudio();
            } catch (Exception e) {
                if (mAudioRecordCallback != null) {
                    mAudioRecordCallback.onEncoderError();
                }
            }
        }
    }
}
