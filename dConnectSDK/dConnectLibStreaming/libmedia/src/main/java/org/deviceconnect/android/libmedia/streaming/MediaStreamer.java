package org.deviceconnect.android.libmedia.streaming;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MediaStreamer {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "MEDIA-STREAMER";

    /**
     * マルチプレクサ.
     */
    private IMediaMuxer mMediaMuxer;

    /**
     * 映像をエンコードするためのクラス.
     */
    private VideoEncoder mVideoEncoder;

    /**
     * 音声をエンコードするためのクラス.
     */
    private AudioEncoder mAudioEncoder;

    /**
     * エンコード処理で発生したイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * エンコード処理状態.
     * <p>
     * エンコード中はtrue、それ以外はfalse
     * </p>
     */
    private boolean mRunningFlag;

    /**
     * コンストラクタ.
     *
     * @param mediaMuxer 出力先のマルチプレクサ
     */
    public MediaStreamer(IMediaMuxer mediaMuxer) {
        if (mediaMuxer == null) {
            throw new IllegalArgumentException("mediaMuxer is null.");
        }
        mMediaMuxer = mediaMuxer;
    }

    /**
     * エンコード処理で発生したイベントを通知するリスナーを設定します.
     *
     * @param listener イベントを通知するリスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 映像をエンコードするエンコーダを設定します.
     *
     * <p>
     * 映像のエンコーダが設定されていない場合には、映像はマルチプレクサに送信されません。
     * </p>
     *
     * @param videoEncoder 映像をエンコードするエンコーダ
     */
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        mVideoEncoder = videoEncoder;
    }

    /**
     * 音声をエンコードするエンコーダを設定します.
     *
     * <p>
     * 音声のエンコーダが設定されていない場合には、音声はマルチプレクサに送信されません。
     * </p>
     *
     * @param audioEncoder 音声をエンコードするエンコーダ
     */
    public void setAudioEncoder(AudioEncoder audioEncoder) {
        mAudioEncoder = audioEncoder;
    }

    /**
     * 映像をエンコードするエンコーダを取得します.
     *
     * <p>
     * 未設定の場合は null を返却します。
     * </p>
     *
     * @return 映像をエンコードするエンコーダ
     */
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }

    /**
     * 音声をエンコードするエンコーダを取得しまs.
     *
     * <p>
     * 未設定の場合は null を返却します。
     * </p>
     *
     * @return 音声をエンコードするエンコーダ
     */
    public AudioEncoder getAudioEncoder() {
        return mAudioEncoder;
    }

    /**
     * エンコード処理を開始します.
     */
    public void start() {
        new Thread(this::startEncoder).start();
    }

    /**
     * エンコード処理を停止します.
     */
    public void stop() {
        new Thread(this::stopEncoder).start();
    }

    /**
     * エンコード処理で発生したイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * エンコード処理の開始を通知します.
         */
        void onStarted();

        /**
         * エンコード処理の停止を通知します.
         */
        void onStopped();

        /**
         * エンコード処理中にエラーが発生したことを通知します.
         *
         * @param e 発生したエラーの例外
         */
        void onError(MediaEncoderException e);
    }

    private void postOnStarted() {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted();
        }
    }

    private void postOnStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped();
        }
    }

    private void postOnError(MediaEncoderException e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * 映像のエンコーダーを開始します.
     *
     * @return エンコーダの開始に成功した場合はtrue、それ以外はfalse
     */
    private boolean startVideoEncoder() {
        CountDownLatch latch = new CountDownLatch(1);
        mVideoEncoder.setCallback(new MediaEncoder.Callback() {
            @Override
            public void onStarted() {
                if (DEBUG) {
                    Log.d(TAG, "VideoEncoder started.");
                }
                latch.countDown();
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.d(TAG, "VideoEncoder stopped.");
                }
            }

            @Override
            public void onFormatChanged(MediaFormat newFormat) {
                mMediaMuxer.onVideoFormatChanged(newFormat);

                if (DEBUG) {
                    int w = newFormat.getInteger(MediaFormat.KEY_WIDTH);
                    int h = newFormat.getInteger(MediaFormat.KEY_HEIGHT);
                    Log.d(TAG, "VideoEncoder size changed. size=" + w + "x" + h);
                }
            }

            @Override
            public void onWriteData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
                mMediaMuxer.onWriteVideoData(encodedData, bufferInfo);
            }

            @Override
            public void onError(MediaEncoderException e) {
                postOnError(e);
            }
        });
        mVideoEncoder.start();

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                postOnError(new MediaEncoderException("Failed to open a video encoder."));
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    /**
     * 音声のエンコーダーを開始します.
     *
     * @return エンコーダの開始に成功した場合はtrue、それ以外はfalse
     */
    private boolean startAudioEncoder() {
        CountDownLatch latch = new CountDownLatch(1);
        mAudioEncoder.setCallback(new MediaEncoder.Callback() {
            @Override
            public void onStarted() {
                if (DEBUG) {
                    Log.d(TAG, "AudioEncoder started.");
                }
                latch.countDown();
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.d(TAG, "AudioEncoder stopped.");
                }
            }

            @Override
            public void onFormatChanged(MediaFormat newFormat) {
                mMediaMuxer.onAudioFormatChanged(newFormat);
            }

            @Override
            public void onWriteData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
                mMediaMuxer.onWriteAudioData(encodedData, bufferInfo);
            }

            @Override
            public void onError(MediaEncoderException e) {
                postOnError(e);
            }
        });
        mAudioEncoder.start();

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                postOnError(new MediaEncoderException("Failed to open a audio encoder."));
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    /**
     * ストリーミングに必要なエンコーダを開始します.
     * <p>
     * 既に開始されている場合には何も行いません。
     * </p>
     */
    private synchronized void startEncoder() {
        if (mRunningFlag) {
            return;
        }
        mRunningFlag = true;

        VideoQuality videoQuality = mVideoEncoder != null ? mVideoEncoder.getVideoQuality() : null;
        AudioQuality audioQuality = mAudioEncoder != null ? mAudioEncoder.getAudioQuality() : null;

        if (!mMediaMuxer.onPrepare(videoQuality, audioQuality)) {
            if (DEBUG) {
                Log.e(TAG, "Failed to prepare a media muxer.");
            }
            postOnError(new MediaEncoderException("Failed to prepare a media muxer."));
            return;
        }

        if (mVideoEncoder != null && !startVideoEncoder()) {
            stopEncoder();
            return;
        }

        if (mAudioEncoder != null && !startAudioEncoder()) {
            stopEncoder();
            return;
        }

        postOnStarted();
    }

    /**
     * エンコーダを停止します.
     * <p>
     * エンコーダが開始されていない場合には何も行いません。
     * </p>
     */
    private synchronized void stopEncoder() {
        if (!mRunningFlag) {
            return;
        }
        mRunningFlag = false;

        if (mAudioEncoder != null) {
            try {
                mAudioEncoder.stop();
            } catch (Exception e) {
                // ignore.
            }
        }

        if (mVideoEncoder != null) {
            try {
                mVideoEncoder.stop();
            } catch (Exception e) {
                // ignore.
            }
        }

        mMediaMuxer.onReleased();

        postOnStopped();
    }
}
