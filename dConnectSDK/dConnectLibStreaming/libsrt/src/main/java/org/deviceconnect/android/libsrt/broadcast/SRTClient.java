package org.deviceconnect.android.libsrt.broadcast;

import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class SRTClient {
    /**
     * データを配信するためのクラス
     */
    private SRTMuxer mSRTMuxer;

    /**
     * ストリーミングを行うためのクラス.
     */
    private MediaStreamer mMediaStreamer;

    /**
     * コンストラクタ.
     */
    public SRTClient(String broadcastURI) {
        mSRTMuxer = new SRTMuxer(broadcastURI);
        mMediaStreamer = new MediaStreamer(mSRTMuxer);
    }

    /**
     * イベントを通知するためのリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mSRTMuxer.setOnEventListener(listener);
        mMediaStreamer.setOnEventListener(listener);
    }

    /**
     * RTMP 配信が動作中か確認します.
     *
     * @return 動作中の場合は true、それ以外は false
     */
    public boolean isRunning() {
        return mMediaStreamer.isRunning();
    }

    /**
     * RTMP のセッションを開始します.
     */
    public void start() {
        mMediaStreamer.start();
    }

    /**
     * RTMP のセッションを停止します.
     */
    public void stop() {
        mMediaStreamer.stop();
    }

    /**
     * 映像エンコーダを設定します.
     *
     * @param videoEncoder エンコーダ
     */
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        if (mMediaStreamer != null) {
            mMediaStreamer.setVideoEncoder(videoEncoder);
        }
    }

    /**
     * 映像エンコーダを取得します.
     *
     * 設定されていない場合には、null を返却します。
     *
     * @return エンコーダ
     */
    public VideoEncoder getVideoEncoder() {
        return mMediaStreamer.getVideoEncoder();
    }

    /**
     * 音声エンコーダを設定します.
     *
     * @param audioEncoder エンコーダ
     */
    public void setAudioEncoder(AudioEncoder audioEncoder) {
        if (mMediaStreamer != null) {
            mMediaStreamer.setAudioEncoder(audioEncoder);
        }
    }

    /**
     * 音声エンコーダを取得します.
     *
     * 設定されていない場合には、null を返却します。
     *
     * @return エンコーダ
     */
    public AudioEncoder getAudioEncoder() {
        return mMediaStreamer.getAudioEncoder();
    }

    /**
     * ミュート設定を行います.
     *
     * @param mute ミュートする場合はtrue、それ以外はfalse
     */
    public void setMute(boolean mute) {
        AudioEncoder audioEncoder = getAudioEncoder();
        if (audioEncoder != null) {
            audioEncoder.setMute(mute);
        }
    }

    /**
     * ミュート設定を確認します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    public boolean isMute() {
        AudioEncoder audioEncoder = getAudioEncoder();
        if (audioEncoder != null) {
            return audioEncoder.isMute();
        }
        return true;
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

    public interface OnEventListener extends MediaStreamer.OnEventListener, SRTMuxer.OnEventListener {
    }
}
