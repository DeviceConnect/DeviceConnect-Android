package org.deviceconnect.android.libsrt.server;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.SRTSocket;

public class SRTSession {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "SRT-SESSION";

    /**
     * ストリーミングを行うためのクラス.
     */
    private MediaStreamer mMediaStreamer;

    /**
     * データを配信するためのクラス
     */
    private final SRTMuxer mSRTMuxer;

    /**
     * コンストラクタ.
     * <p>
     * デフォルトでは、Mpeg2TsMuxer を設定します。
     * </p>
     */
    public SRTSession() {
        this(new Mpeg2TsMuxer());
    }

    /**
     * コンストラクタ .
     * @param muxer 配信処理を行う Muxer
     */
    public SRTSession(SRTMuxer muxer) {
        if (muxer == null) {
            throw new IllegalArgumentException("muxer is not set.");
        }

        mSRTMuxer = muxer;
        mMediaStreamer = new MediaStreamer(mSRTMuxer);
        mMediaStreamer.setOnEventListener(new MediaStreamer.OnEventListener() {
            @Override
            public void onStarted() {
                if (DEBUG) {
                    Log.d(TAG, "MediaStreamer started.");
                }
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.d(TAG, "MediaStreamer stopped.");
                }
            }

            @Override
            public void onError(MediaEncoderException e) {
                if (DEBUG) {
                    Log.e(TAG, "Error occurred on MediaStreamer.", e);
                }
            }
        });
    }

    /**
     * SRT のセッションを開始します.
     */
    public void start() {
        mMediaStreamer.start();
    }

    /**
     * SRT のセッションを停止します.
     */
    public void stop() {
        mMediaStreamer.stop();
    }

    /**
     * 映像エンコーダーを設定します.
     *
     * @param videoEncoder 映像エンコーダー
     */
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        mMediaStreamer.setVideoEncoder(videoEncoder);
    }

    /**
     * 音声エンコーダーを設定します.
     *
     * @param audioEncoder 音声エンコーダー
     */
    public void setAudioEncoder(AudioEncoder audioEncoder) {
        mMediaStreamer.setAudioEncoder(audioEncoder);
    }

    /**
     * 映像エンコーダーを取得します.
     *
     * @return 映像エンコーダー
     */
    public VideoEncoder getVideoEncoder() {
        return mMediaStreamer.getVideoEncoder();
    }

    /**
     * 音声エンコーダーを取得します.
     *
     * @return 音声エンコーダー
     */
    public AudioEncoder getAudioEncoder() {
        return mMediaStreamer.getAudioEncoder();
    }

    /**
     * 送信先のソケットを追加します.
     *
     * @param socket 追加するソケット
     */
    public void addSRTClientSocket(SRTSocket socket) {
        mSRTMuxer.addSRTClientSocket(socket);
    }

    /**
     * 送信先のソケットを削除します.
     *
     * @param socket 削除するソケット
     */
    public void removeSRTClientSocket(SRTSocket socket) {
        mSRTMuxer.removeSRTClientSocket(socket);
    }

    public byte[] getAudioRawCache() {
        return ((Mpeg2TsMuxer) mSRTMuxer).getAudioRawCache();
    }
}
