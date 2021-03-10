package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.filter.Filter;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.nio.ByteBuffer;

/**
 * マイクから音声データを取得して、エンコードするクラス.
 */
public class MicAACLATMEncoder extends AudioEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MIC-AAC-ENCODER";

    /**
     * 音声のエンコード設定.
     */
    private final MicAudioQuality mAudioQuality = new MicAudioQuality("audio/mp4a-latm");

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
            mMediaCodec.queueInputBuffer(index, 0, 0, getPTSUs(), 0);
        } else if (isMute()) {
            // ミュート設定の場合には、AudioRecord からデータを取得しない
            int length = mMuteBuffer.length;
            if (inputData.remaining() < length) {
                length = inputData.remaining();
            }
            inputData.put(mMuteBuffer, 0, length);
            mMediaCodec.queueInputBuffer(index, 0, length, getPTSUs(), 0);
        } else {
            mAudioThread.add(() -> {
                int len = mAudioRecord.read(inputData, mBufferSize);
                if (DEBUG && len < 0) {
                    Log.e(TAG, "An error occurred with the AudioRecord API ! len=" + len);
                }
                Filter filter = mAudioQuality.getFilter();
                if (filter != null && len > 0) {
                    filter.onProcessing(inputData, len);
                }
                mMediaCodec.queueInputBuffer(index, 0, len, getPTSUs(), 0);
            });
        }
    }

    /**
     * 音声を録音するためのスレッド.
     */
    private static class AudioRecordThread extends QueueThread<Runnable> {
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
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

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
     * 音声ソースを取得します.
     *
     * @return 音声ソース
     */
    private int getAudioSource() {
        switch (mAudioQuality.getSource()) {
            case DEFAULT:
                return MediaRecorder.AudioSource.DEFAULT;
            case MIC:
                return MediaRecorder.AudioSource.MIC;
            case APP:
            default:
                throw new RuntimeException("Audio source is not supported.");
        }
    }

    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;

    /**
     * AudioRecord を開始します.
     */
    private void startAudioRecord() {
        int minBufferSize = AudioRecord.getMinBufferSize(mAudioQuality.getSamplingRate(),
                mAudioQuality.getChannel(), mAudioQuality.getFormat()) * 2;

        mBufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
        if (mBufferSize < minBufferSize) {
            mBufferSize = ((minBufferSize / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
        }

        if (DEBUG) {
            Log.d(TAG, "AudioQuality: " + mAudioQuality);
            Log.d(TAG, "  bufferSize: " + mBufferSize);
        }

        mMuteBuffer = new byte[mBufferSize];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioRecord.Builder builder = new AudioRecord.Builder()
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(mAudioQuality.getFormat())
                            .setSampleRate(mAudioQuality.getSamplingRate())
                            .setChannelMask(mAudioQuality.getChannel())
                            .build())
                    .setBufferSizeInBytes(mBufferSize);

            switch (mAudioQuality.getSource()) {
                case DEFAULT:
                case MIC:
                    builder.setAudioSource(getAudioSource());
                    break;
                case APP:
                    AudioPlaybackCaptureConfiguration config = mAudioQuality.getCaptureConfig();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && config != null) {
                        builder.setAudioPlaybackCaptureConfig(config);
                    } else {
                        throw new RuntimeException("Audio capture settings is invalid.");
                    }
                    break;
            }
            mAudioRecord = builder.build();
        } else {
            mAudioRecord = new AudioRecord(getAudioSource(),
                    mAudioQuality.getSamplingRate(),
                    mAudioQuality.getChannel(),
                    mAudioQuality.getFormat(),
                    mBufferSize);
        }

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

        Filter filter = mAudioQuality.getFilter();
        if (filter != null) {
            filter.onRelease();
        }

        if (mEchoCanceler != null) {
            mEchoCanceler.release();
            mEchoCanceler = null;
        }
    }
}
