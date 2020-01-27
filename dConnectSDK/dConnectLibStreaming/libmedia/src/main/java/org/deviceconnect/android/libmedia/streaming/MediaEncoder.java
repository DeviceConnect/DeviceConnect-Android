package org.deviceconnect.android.libmedia.streaming;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

public abstract class MediaEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MEDIA-ENCODER";

    /**
     * エンコード処理を行うためのクラス.
     */
    protected MediaCodec mMediaCodec;

    private StartingThread mStartingThread;
    private StoppingThread mStoppingThread;

    /**
     * エンコード処理を通知するコールバック.
     */
    private Callback mCallback;

    /**
     * エンコーダーの準備を行います.
     *
     * <p>
     * エンコーダで使用する MediaCodec を作成します。
     * </p>
     *
     * @throws IOException エンコーダーの準備に失敗した場合に発生
     */
    protected abstract void prepare() throws IOException;

    /**
     * エンコードを開始する時に呼び出されます。
     */
    protected abstract void startRecording();

    /**
     * エンコードを開始する時に呼び出されます。
     */
    protected abstract void stopRecording();

    /**
     * エンコーダーの破棄処理を行います.
     */
    protected abstract void release();

    /**
     * コールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * エンコード処理を開始します.
     */
    public synchronized void start() {
        if (mMediaCodec != null) {
            if (DEBUG) {
                Log.w(TAG, "MediaEncoder is already started.");
            }
            return;
        }

        if (mStartingThread != null) {
            if (DEBUG) {
                Log.w(TAG, "MediaEncoder is starting.");
            }
            return;
        }

        mStartingThread = new StartingThread();
        mStartingThread.setName("Encoder-Starting-Thread");
        mStartingThread.start();
    }

    /**
     * エンコード処理を停止します.
     */
    public synchronized void stop() {
        if (mMediaCodec == null) {
            if (DEBUG) {
                Log.w(TAG, "MediaEncoder is already stopped.");
            }
            return;
        }

        if (mStoppingThread != null) {
            if (DEBUG) {
                Log.w(TAG, "MediaEncoder is stopping.");
            }
            return;
        }

        mStoppingThread = new StoppingThread();
        mStoppingThread.setName("Encoder-Stopping-Thread");
        mStoppingThread.start();
    }

    /**
     * エンコーダを再起動の処理を行います.
     */
    public synchronized void restart() {
        if (mStartingThread == null) {
            if (DEBUG) {
                Log.w(TAG, "MediaEncoder has not started.");
            }
            return;
        }

        stopEncoder();
        startEncoder();
    }

    /**
     * エンコーダの開始イベントを通知します.
     */
    private void postOnStarted() {
        if (mCallback != null) {
            mCallback.onStarted();
        }
    }

    /**
     * エンコーダの停止イベントを通知します.
     */
    private void postOnStopped() {
        if (mCallback != null) {
            mCallback.onStopped();
        }
    }

    /**
     * エンコーダのエラーイベントを通知します.
     *
     * @param e エラー原因の例外
     */
    protected void postOnError(MediaEncoderException e) {
        if (mCallback != null) {
            mCallback.onError(e);
        }
    }

    /**
     * エンコードしたデータを通知します.
     *
     * @param encodedData エンコードされたデータ
     * @param bufferInfo エンコード情報
     */
    protected void postOnWriteData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (mCallback != null) {
            try {
                mCallback.onWriteData(encodedData, bufferInfo);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }

    /**
     * エンコードフォーマットが変更されたことを通知します.
     *
     * @param newFormat 新しいフォーマット
     */
    private void postOnFormatChanged(MediaFormat newFormat) {
        if (mCallback != null) {
            try {
                mCallback.onFormatChanged(newFormat);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }

    /**
     * MediaCodec にエンコードするためのデータを書き込みます.
     *
     * <p>
     * 指定された index を引数にして、{@link MediaCodec#queueInputBuffer(int, int, int, long, int)}
     * を呼び出してください。
     * </p>
     *
     * @param inputData 書き込むデータ
     * @param index バッファのインデックス
     */
    protected void onInputData(ByteBuffer inputData, int index) {
    }

    /**
     * エンコーダの処理を開始処理を行います.
     *
     * @return 開始処理に成功した場合はtrue、それ以外はfalse
     */
    private boolean startEncoder() {
        try {
            prepare();
        } catch (Exception e) {
            postOnError(new MediaEncoderException(e));
            return false;
        }

        try {
            startRecording();
        } catch (Exception e) {
            postOnError(new MediaEncoderException(e));
            return false;
        }

        if (mMediaCodec != null) {
            mMediaCodec.setCallback(mMediaCodecCallback);
            mMediaCodec.start();
        }

        return true;
    }

    /**
     * エンコーダの処理を停止します.
     */
    private void stopEncoder() {
        try {
            stopRecording();
        } catch (Exception e) {
            // ignore
        }

        try {
            release();
        } catch (Exception e) {
            // ignore.
        }
    }

    private final MediaCodec.Callback mMediaCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            try {
                ByteBuffer inputData = mMediaCodec.getInputBuffer(index);
                onInputData(inputData, index);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "MediaCodec.Callback#onInputBufferAvailable", e);
                }
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            try {
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(index);
                info.presentationTimeUs = getPTSUs();
                postOnWriteData(encodedData, info);
                mMediaCodec.releaseOutputBuffer(index, false);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "MediaCodec.Callback#onOutputBufferAvailable", e);
                }
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            postOnError(new MediaEncoderException(e));
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            postOnFormatChanged(format);
        }
    };

    /**
     * エンコーダの開始処理を行うスレッド.
     */
    private class StartingThread extends Thread {
        @Override
        public void run() {
            if (startEncoder()) {
                postOnStarted();
            }
        }
    }

    /**
     * エンコーダの停止処理を行うスレッド.
     */
    private class StoppingThread extends Thread {
        @Override
        public void run() {
            stopEncoder();
            mStartingThread = null;
            mStoppingThread = null;
            postOnStopped();
        }
    }

    /**
     * エンコード処理を通知するコールバック.
     */
    public interface Callback {
        /**
         * エンコード開始を通知します.
         */
        void onStarted();

        /**
         * エンコード停止を通知します.
         */
        void onStopped();

        /**
         * フォーマットが変更要求を通知します.
         *
         * @param newFormat 新しいフォーマット
         */
        void onFormatChanged(MediaFormat newFormat);

        /**
         * データの書き込み要求を通知します.
         *
         * @param encodedData データ
         * @param bufferInfo データ情報
         */
        void onWriteData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo);

        /**
         * エンコーダでエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(MediaEncoderException e);
    }

    /**
     * 前回のプレゼンテーションを格納する変数.
     */
    private long mPrevOutputPTSUs;

    /**
     * プレゼンテーションタイムを取得します.
     *
     * @return プレゼンテーションタイム
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < mPrevOutputPTSUs) {
            result = (mPrevOutputPTSUs - result) + result;
        }
        mPrevOutputPTSUs = result;
        return result;
    }
}
