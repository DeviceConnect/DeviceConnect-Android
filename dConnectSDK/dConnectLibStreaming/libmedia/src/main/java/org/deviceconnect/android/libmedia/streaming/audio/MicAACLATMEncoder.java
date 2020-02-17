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
    private static final String TAG = "MIC-AAC-ENCODER";

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
     * マイクから音声をレコードするクラス.
     */
    private AudioRecord mAudioRecord;

    /**
     * エコーキャンセラー.
     */
    private AcousticEchoCanceler mEchoCanceler;

    /**
     * AudioRecord で使用するバッファサイズ.
     */
    private int mBufferSize;

    /**
     * 録音を行うスレッド.
     */
    private AudioRecordThread mAudioThread;

    /**
     * ミュート用の音声データを確保するバッファ.
     */
    private byte[] mMuteBuffer;

    @Override
    public AudioQuality getAudioQuality() {
        return mAudioQuality;
    }

    @Override
    protected synchronized void startRecording() {
        super.startRecording();

        try {
            startAudioRecord();
        } catch (Exception e) {
            // AudioRecord の初期化中にエラーが発生した場合はとりあえず停止しておく
            stopAudioRecord();
        }
    }

    @Override
    protected synchronized void stopRecording() {
        stopAudioRecord();
        super.stopRecording();
    }

    @Override
    protected void onInputData(ByteBuffer inputData, int index) {
        inputData.clear();

        if (mAudioRecord == null || mAudioThread == null || mMuteBuffer == null) {
            mMediaCodec.queueInputBuffer(index, 0, 0, System.nanoTime() / 1000, 0);
        } else if (isMute()) {
            // ミュート設定の場合には、AudioRecord からデータを取得しない
            inputData.put(mMuteBuffer);
            mMediaCodec.queueInputBuffer(index, 0, mBufferSize, System.nanoTime() / 1000, 0);
        } else {
            mAudioThread.add(() -> {
                int len = mAudioRecord.read(inputData, mBufferSize);
                if (len < 0) {
                    if (DEBUG) {
                        Log.e(TAG, "An error occurred with the AudioRecord API ! len=" + len);
                    }
                }
                inputData.flip();
                mMediaCodec.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0);
            });
        }
    }

    /**
     * 音声を録音するためのスレッド.
     */
    private class AudioRecordThread extends QueueThread<Runnable> {
        /**
         * スレッドを終了します.
         */
        void terminate() {
            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    get().run();
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }
    }

    /**
     * AudioRecord を開始します.
     */
    private void startAudioRecord() {
        AudioQuality audioQuality = getAudioQuality();

        mBufferSize = AudioRecord.getMinBufferSize(audioQuality.getSamplingRate(),
                audioQuality.getChannel(), audioQuality.getFormat()) * 4;

        if (DEBUG) {
            Log.d(TAG, "AudioQuality: " + audioQuality);
        }

        mMuteBuffer = new byte[mBufferSize];

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                audioQuality.getSamplingRate(),
                audioQuality.getChannel(),
                audioQuality.getFormat(),
                mBufferSize);

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

        mAudioThread = new AudioRecordThread();
        mAudioThread.setName("MicAACLATMEncoder");
        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
    }

    /**
     * AudioRecord を停止します.
     */
    private void stopAudioRecord() {
        if (mAudioThread != null) {
            mAudioThread.terminate();
            mAudioThread = null;
        }

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
    }
}
