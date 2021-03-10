package org.deviceconnect.android.libsrt.broadcast;

import android.os.Handler;
import android.os.HandlerThread;

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
    private Handler mRetryHandler;

    /**
     * リトライ数.
     */
    private int mRetryCount;

    /**
     * リトライ回数.
     */
    private int mMaxRetryCount = 0;

    /**
     * リトライを行うインターバル.
     */
    private int mRetryInterval = 4000;

    /**
     * リトライフラグ.
     * <p>
     * このフラグがtrueの場合はリトライ処理中になります。
     * </p>
     */
    private boolean mRetryFlag;

    /**
     * イベントを送信するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * コンストラクタ.
     */
    public SRTClient(String broadcastURI) {
        mSRTMuxer = new SRTMuxer(broadcastURI);
        mSRTMuxer.setOnEventListener(new SRTMuxer.OnEventListener() {
            @Override
            public void onConnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onConnected();
                }
            }

            @Override
            public void onDisconnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onDisconnected();
                }
            }

            @Override
            public void onNewBitrate(long bitrate) {
                if (mOnEventListener != null) {
                    mOnEventListener.onNewBitrate(bitrate);
                }
            }

            @Override
            public void onError(MediaEncoderException e) {
                error(e);
            }
        });

        mMediaStreamer = new MediaStreamer(mSRTMuxer);
        mMediaStreamer.setOnEventListener(new MediaStreamer.OnEventListener() {
            @Override
            public void onStarted() {
                mRetryCount = 0;

                if (mOnEventListener != null) {
                    mOnEventListener.onStarted();
                }
            }

            @Override
            public void onStopped() {
                if (mOnEventListener != null) {
                    mOnEventListener.onStopped();
                }
            }

            @Override
            public void onError(MediaEncoderException e) {
                error(e);
            }
        });
    }

    /**
     * エラー処理を行います.
     *
     * @param e エラー原因の例外
     */
    private synchronized void error(MediaEncoderException e) {
        if (mRetryFlag) {
            // リトライ処理中なので、連続で発生したエラーは無視する。
            return;
        }

        if (mRetryCount < mMaxRetryCount) {
            mRetryCount++;
            mRetryFlag = true;

            mMediaStreamer.stop();

            if (mRetryHandler != null) {
                mRetryHandler.postDelayed(() -> {
                    if (mRetryHandler != null) {
                        mMediaStreamer.start();
                    }
                    mRetryFlag = false;
                }, mRetryInterval);
            }
        } else {
            stop();

            if (mOnEventListener != null) {
                mOnEventListener.onError(e);
            }
        }
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
        mOnEventListener = listener;
    }

    /**
     * RTMP 配信が動作中か確認します.
     *
     * @return 動作中の場合は true、それ以外は false
     */
    public boolean isRunning() {
        return mRetryHandler != null;
    }

    /**
     * RTMP のセッションを開始します.
     */
    public synchronized void start() {
        if (mRetryHandler != null) {
            return;
        }

        HandlerThread thread = new HandlerThread("rtmp-retry-thread");
        thread.start();
        mRetryHandler = new Handler(thread.getLooper());
        mRetryCount = mMaxRetryCount;
        mRetryFlag = false;
        mMediaStreamer.start();
    }

    /**
     * RTMP のセッションを停止します.
     */
    public synchronized void stop() {
        if (mRetryHandler != null) {
            mRetryHandler.getLooper().quit();
            mRetryHandler = null;
        }
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

    /**
     * SRTClient で発生したイベントを通知するリスナー.
     */
    public interface OnEventListener extends MediaStreamer.OnEventListener, SRTMuxer.OnEventListener {
    }
}
