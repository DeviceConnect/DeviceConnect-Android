package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.nio.ByteBuffer;

/**
 * マイクから音声データを取得して、エンコードするクラス.
 */
public class MicAACLATMEncoder extends AudioEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MIC-ENCODER";

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
     * 音声のエンコード設定.
     */
    private final AudioQuality mAudioQuality = new AudioQuality("audio/mp4a-latm") {
        @Override
        public int[] getSupportSamplingRates() {
            return SUPPORT_AUDIO_SAMPLING_RATES;
        }
    };

    /**
     * マイクから音声を入力するためのスレッド.
     */
    private MicRecordThread mMicRecordThread;

    @Override
    public AudioQuality getAudioQuality() {
        return mAudioQuality;
    }

    @Override
    protected void release() {
        super.release();
    }

    @Override
    protected synchronized void startRecording() {
        super.startRecording();

        if (mMicRecordThread != null) {
            mMicRecordThread.terminate();
        }
        mMicRecordThread = new MicRecordThread();
        mMicRecordThread.setName("MIC-RECORD");
        mMicRecordThread.setPriority(Thread.MAX_PRIORITY);
        mMicRecordThread.start();
    }

    @Override
    protected synchronized void stopRecording() {
        if (mMicRecordThread != null) {
            mMicRecordThread.terminate();
            mMicRecordThread = null;
        }
        super.stopRecording();
    }

    @Override
    protected void onInputData(ByteBuffer inputData, int index) {
        while (mMicRecordThread == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                return;
            }
        }
        mMicRecordThread.add(new MediaCodecBuffer(inputData, index));
    }

    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);
//        new Thread(this::restart).start();
    }

    /**
     * 音声レコード用のスレッド.
     */
    private class MicRecordThread extends QueueThread<MediaCodecBuffer> {
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
                try {
                    mAudioRecord.release();
                } catch (Exception e) {
                    // ignore.
                }
                mAudioRecord = null;
            }

            if (mEchoCanceler != null) {
                mEchoCanceler.release();
                mEchoCanceler = null;
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
        private void recordAudio() throws InterruptedException {
            AudioQuality audioQuality = getAudioQuality();

            int bufferSize = AudioRecord.getMinBufferSize(audioQuality.getSamplingRate(),
                    audioQuality.getChannel(), audioQuality.getFormat()) * 4;

            if (DEBUG) {
                Log.d(TAG, "AudioQuality: " + audioQuality);
            }

            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    audioQuality.getSamplingRate(),
                    audioQuality.getChannel(),
                    audioQuality.getFormat(),
                    bufferSize);

            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                postOnError(new MediaEncoderException("AudioRecord is already initialized."));
                return;
            }

            if (mAudioQuality.isUseAEC() && AcousticEchoCanceler.isAvailable()) {
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

            mAudioRecord.startRecording();

            while (!mStopFlag) {
                MediaCodecBuffer mediaCodecBuffer = get();
                ByteBuffer buffer = mediaCodecBuffer.mByteBuffer;
                buffer.clear();
                int len = 0;
                if (!isMute()) {
                    len = mAudioRecord.read(buffer, bufferSize);
                    if (len < 0) {
                        if (DEBUG) {
                            Log.e(TAG, "An error occurred with the AudioRecord API ! len=" + len);
                        }
                    }
                    buffer.flip();
                }
                mMediaCodec.queueInputBuffer(mediaCodecBuffer.mIndex, 0, len, System.nanoTime()/1000, 0);
            }
        }

        @Override
        public void run() {
            try {
                recordAudio();
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }
    }

    private class MediaCodecBuffer {
        ByteBuffer mByteBuffer;
        int mIndex;

        MediaCodecBuffer(ByteBuffer byteBuffer, int index) {
            mByteBuffer = byteBuffer;
            mIndex = index;
        }
    }
}
