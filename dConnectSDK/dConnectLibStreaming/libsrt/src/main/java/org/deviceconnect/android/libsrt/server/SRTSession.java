package org.deviceconnect.android.libsrt.server;

import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.util.Mpeg2TsMuxer;

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
     * SRTSession のイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * コンストラクタ.
     * <p>
     * デフォルトでは、Mpeg2TsMuxer を設定します。
     * </p>
     */
    public SRTSession() {
        this(new SRTMuxer(), null);
    }

    /**
     * コンストラクタ .
     * <p>
     * デフォルトでは、Mpeg2TsMuxer を設定します。
     * </p>
     * @param listener リスナー
     */
    public SRTSession(OnEventListener listener) {
        this(new SRTMuxer(), listener);
    }

    /**
     * コンストラクタ .
     * @param muxer 配信処理を行う Muxer
     */
    public SRTSession(SRTMuxer muxer, OnEventListener listener) {
        if (muxer == null) {
            throw new IllegalArgumentException("muxer is not set.");
        }

        mOnEventListener = listener;

        mSRTMuxer = muxer;
        mMediaStreamer = new MediaStreamer(mSRTMuxer);
        mMediaStreamer.setOnEventListener(listener);
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

    /**
     * VideoEncoder を再リスタートします.
     * <p>
     * VideoEncoder が設定されていない場合には何もしません。
     * </p>
     */
    public void restartVideoEncoder() {
        VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
        if (videoEncoder != null) {
            videoEncoder.restart();
        }
    }

    /**
     * AudioEncoder を再リスタートします.
     * <p>
     * AudioEncoder が設定されていない場合には何もしません。
     * </p>
     */
    public void restartAudioEncoder() {
        AudioEncoder audioEncoder = mMediaStreamer.getAudioEncoder();
        if (audioEncoder != null) {
            audioEncoder.restart();
        }
    }

    /**
     * SRTSession のイベントを通知するリスナー.
     */
    public interface OnEventListener extends MediaStreamer.OnEventListener {
    }
}
