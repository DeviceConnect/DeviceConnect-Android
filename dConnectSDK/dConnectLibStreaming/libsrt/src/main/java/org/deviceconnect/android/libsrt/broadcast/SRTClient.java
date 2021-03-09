package org.deviceconnect.android.libsrt.broadcast;

import android.os.Handler;
import android.os.Looper;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class SRTClient {
    /**
     * データを配信するためのクラス
     */
    private final SRTMuxer mSRTMuxer;

    /**
     * ストリーミングを行うためのクラス.
     */
    private final MediaStreamer mMediaStreamer;

    /**
     * ハンドラ.
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * リトライ数.
     */
    private int mRetryCount;

    /**
     * リトライ回数.
     */
    private int mMaxRetryCount = 3;

    /**
     * リトライを行うインターバル.
     */
    private int mRetryInterval = 2000;

    /**
     * リトライフラグ.
     * <p>
     * このフラグがtrueの場合はリトライ処理中になります。
     * </p>
     */
    private boolean mRetryFlag;

    /**
     * 起動フラグ.
     */
    private boolean mRunnableFlag;

    /**
     * コンストラクタ.
     */
    public SRTClient(String broadcastURI) {
        mSRTMuxer = new SRTMuxer(broadcastURI);
        mMediaStreamer = new MediaStreamer(mSRTMuxer);
    }

    /**
     * リトライ回数を設定します.
     *
     * @param maxRetryCount リトライ回数
     */
    public void setMaxRetryCount(int maxRetryCount) {
        mMaxRetryCount = maxRetryCount;
    }

    /**
     * リトライ回数を取得します.
     *
     * @return リトライ回数
     */
    public int getMaxRetryCount() {
        return mMaxRetryCount;
    }

    /**
     * リトライのインターバルを設定します.
     *
     * @param interval リトライを行うインターバル
     */
    public void setRetryInterval(int interval) {
        mRetryInterval = interval;
    }

    /**
     * リトライのインターバルを取得します.
     *
     * @return リトライのインターバル
     */
    public int getRetryInterval() {
        return mRetryInterval;
    }

    /**
     * イベントを通知するためのリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mMediaStreamer.setOnEventListener(listener);
        mSRTMuxer.setOnEventListener(new SRTMuxer.OnEventListener() {
            @Override
            public void onConnected() {
                // 接続できたのでリトライ数を初期化
                mRetryCount = 0;
                if (listener != null) {
                    listener.onConnected();
                }
            }

            @Override
            public void onDisconnected() {
                if (mRetryFlag) {
                    // エラーが発生して切断された時も、このイベントが呼び出されるので
                    // リトライの処理を行っている間はリスナーに通知しないようにブロックする。
                    return;
                }

                if (listener != null) {
                    listener.onDisconnected();
                }
            }

            @Override
            public void onNewBitrate(long bitrate) {
                if (listener != null) {
                    listener.onNewBitrate(bitrate);
                }
            }

            @Override
            public void onError(MediaEncoderException e) {
                if (mRetryFlag) {
                    return;
                }

                if (mRetryCount < mMaxRetryCount) {
                    mRetryCount++;
                    mRetryFlag = true;

                    mMediaStreamer.stop();

                    mHandler.postDelayed(() -> {
                        mRetryFlag = false;
                        if (mRunnableFlag) {
                            mMediaStreamer.start();
                        }
                    }, mRetryInterval);
                } else {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        });
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
        mRetryCount = mMaxRetryCount;
        mRunnableFlag = true;
        mMediaStreamer.start();
    }

    /**
     * RTMP のセッションを停止します.
     */
    public void stop() {
        mRunnableFlag = false;
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
