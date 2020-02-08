package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.io.IOException;
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
    private AudioThread mAudioThread;

    @Override
    public AudioQuality getAudioQuality() {
        return mAudioQuality;
    }

    @Override
    protected void prepare() throws IOException {
        if (isMute()) {
            // ミュートの場合には、MediaCodec を作成させないようにします。
            return;
        }
        super.prepare();
    }

    @Override
    protected synchronized void startRecording() {
        if (isMute()) {
            // ミュートの場合には、AudioCodec を作成させないようにします。
            return;
        }
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

        // ミュート設定の場合には、AudioRecord からデータを取得しない
        if (!isMute() && mAudioRecord != null && mAudioThread != null) {
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
        } else {
            mMediaCodec.queueInputBuffer(index, 0, 0, System.nanoTime() / 1000, 0);
        }
    }

    /**
     * 音声を録音するためのスレッド.
     */
    private class AudioThread extends QueueThread<Runnable> {
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

        mAudioThread = new AudioThread();
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
