package org.deviceconnect.android.libsrt.client;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsConstants;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsPacketExtractor;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.SRTStats;
import org.deviceconnect.android.libsrt.client.decoder.audio.AACDecoder;
import org.deviceconnect.android.libsrt.client.decoder.audio.AudioDecoder;
import org.deviceconnect.android.libsrt.client.decoder.video.H264Decoder;
import org.deviceconnect.android.libsrt.client.decoder.video.H265Decoder;
import org.deviceconnect.android.libsrt.client.decoder.video.VideoDecoder;
import org.deviceconnect.android.libsrt.util.SRTSocketThread;

import java.util.Map;

/**
 * SRT からのデータを再生するためのクラス.
 */
public class SRTPlayer {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "SRT-PLAYER";

    /**
     * 統計データをログ出力するインターバルのデフォルト値. 単位はミリ秒.
     */
    private static final long DEFAULT_STATS_INTERVAL = 5000;

    /**
     * SRT の通信を行うクラス.
     */
    private SRTSocketThread mSRTSocketThread;

    /**
     * クライアントのソケットに設定するオプション.
     */
    private Map<Integer, Object> mCustomSocketOptions;

    /**
     * 接続先の URI.
     */
    private String mUri;

    /**
     * TS パケットからストリームデータを抽出するクラス.
     */
    private TsPacketExtractor mPacketExtractor;

    /**
     * 映像の描画先の Surface.
     */
    private Surface mSurface;

    /**
     * 映像をデコードするクラス.
     */
    private VideoDecoder mVideoDecoder;

    /**
     * 音声をデコードするクラス.
     */
    private AudioDecoder mAudioDecoder;

    /**
     * 統計データをログ出力するインターバル. 単位はミリ秒.
     */
    private long mStatsInterval = DEFAULT_STATS_INTERVAL;

    /**
     * 統計データをログに出力フラグ.
     *
     * <p>
     * trueの場合は、ログを出力します。
     * </p>
     */
    private boolean mShowStats;

    /**
     * SRT プレイヤーのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * SRT プレイヤーのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 接続先の URI を設定します.
     *
     * @param uri URI
     */
    public void setUri(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri is null.");
        }
        mUri = uri;
    }

    /**
     * 描画先の Surface を設定します.
     *
     * @param surface 描画先の Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * ソケットに反映するオプションを設定します.
     *
     * <p>
     * オプションはサーバ側のソケットがコネクトされる前に設定されます.
     * </p>
     *
     * @param socketOptions ソケットに反映するオプション
     */
    public void setSocketOptions(Map<Integer, Object> socketOptions) {
        mCustomSocketOptions = socketOptions;
    }

    /**
     * SRT ソケットにオプションを設定します.
     *
     * <p>
     * Binding が post のオプションのみ、ソケットが接続された後にもオプションを設定することができます。
     * </p>
     *
     * @param option オプション
     * @param value 値
     */
    public synchronized void setSocketOption(int option, Object value) {
        if (mSRTSocketThread != null) {
            mSRTSocketThread.setSocketOption(option, value);
        }
    }

    /**
     * SRT プレイヤーを開始します.
     */
    public synchronized void start() {
        if (mSRTSocketThread != null) {
            return;
        }

        if (mUri == null) {
            throw new IllegalArgumentException("uri is not set.");
        }

        Uri uri = Uri.parse(mUri);
        if (!"srt".equals(uri.getScheme())) {
            throw new IllegalArgumentException("uri scheme is not srt.");
        }

        mPacketExtractor = new TsPacketExtractor();
        mPacketExtractor.setCallback(mCallback);
        mPacketExtractor.start();

        mSRTSocketThread = new SRTSocketThread(uri.getHost(), uri.getPort());
        mSRTSocketThread.setSocketOptions(mCustomSocketOptions);
        mSRTSocketThread.setOnEventListener(mOnClientEventListener);
        mSRTSocketThread.setStatsInterval(mStatsInterval);
        mSRTSocketThread.setShowStats(mShowStats);
        mSRTSocketThread.start();
    }

    /**
     * SRT プレイヤーを停止します.
     */
    public synchronized void stop() {
        if (mPacketExtractor != null) {
            mPacketExtractor.terminate();
            mPacketExtractor = null;
        }

        if (mSRTSocketThread != null) {
            mSRTSocketThread.stop();
            mSRTSocketThread = null;
        }
    }

    /**
     * SRT 統計データの LogCat への表示設定を行います.
     *
     * @param showStats LogCat に表示する場合はtrue、それ以外はfalse
     */
    public synchronized void setShowStats(boolean showStats) {
        mShowStats = showStats;

        // 既にクライアントが開始されている場合は、タイマーの設定を行います。
        if (mSRTSocketThread != null) {
            mSRTSocketThread.setStatsInterval(mStatsInterval);
            mSRTSocketThread.setShowStats(showStats);
        }
    }

    /**
     * SRT 統計データの LogCat へ表示するインターバルを設定します.
     *
     * <p>
     * {@link #setShowStats(boolean)} の前に実行すること.
     * </p>
     *
     * @param interval インターバル. 単位はミリ秒
     */
    public void setStatsInterval(long interval) {
        mStatsInterval = interval;
    }

    private void postOnConnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onConnected();
        }
    }

    private void postOnDisconnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onDisconnected();
        }
    }

    private void postOnReady() {
        if (mOnEventListener != null) {
            mOnEventListener.onReady();
        }
    }

    private void postOnSizeChanged(int width, int height) {
        if (mOnEventListener != null) {
            mOnEventListener.onSizeChanged(width, height);
        }
    }

    private void postOnError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    private void postOnStats(SRTStats stats) {
        if (mOnEventListener != null) {
            mOnEventListener.onStats(stats);
        }
    }

    /**
     * デコーダを作成します.
     *
     * @param streamType ストリームタイプ
     */
    private synchronized void createDecoder(int streamType) {
        switch (streamType) {
            case TsConstants.STREAM_TYPE_VIDEO_H264:
            case TsConstants.STREAM_TYPE_VIDEO_H265:
            {
                if (mVideoDecoder != null) {
                    if (DEBUG) {
                        Log.w(TAG, "VideoDecoder has already existed.");
                    }
                    return;
                }

                if (mSurface == null) {
                    if (DEBUG) {
                        Log.w(TAG, "Surface is not set.");
                    }
                    return;
                }

                if (streamType == TsConstants.STREAM_TYPE_VIDEO_H264) {
                    mVideoDecoder = new H264Decoder();
                } else {
                    mVideoDecoder = new H265Decoder();
                }
                mVideoDecoder.setSurface(mSurface);
                mVideoDecoder.setErrorCallback(SRTPlayer.this::postOnError);
                mVideoDecoder.setEventCallback(SRTPlayer.this::postOnSizeChanged);
                mVideoDecoder.onInit();
            }
                break;
            case TsConstants.STREAM_TYPE_AUDIO_AAC:
            {
                if (mAudioDecoder != null) {
                    if (DEBUG) {
                        Log.w(TAG, "AudioEncoder has already existed.");
                    }
                    return;
                }

                mAudioDecoder = new AACDecoder();
                mAudioDecoder.setErrorCallback(SRTPlayer.this::postOnError);
                mAudioDecoder.onInit();
            }
                break;
        }
    }

    /**
     * デコーダを削除します.
     */
    private synchronized void releaseDecoder() {
        if (mVideoDecoder != null) {
            try {
                mVideoDecoder.onReleased();
            } catch (Exception e) {
                // ignore
            }
            mVideoDecoder = null;
        }

        if (mAudioDecoder != null) {
            try {
                mAudioDecoder.onReleased();
            } catch (Exception e) {
                // ignore.
            }
            mAudioDecoder = null;
        }
    }

    private final SRTSocketThread.OnEventListener mOnClientEventListener = new SRTSocketThread.OnEventListener() {
        @Override
        public void onReceived(byte[] data, int dataLength) {
            if (mPacketExtractor != null) {
                mPacketExtractor.add(data, dataLength);
            }
        }

        @Override
        public void onConnected() {
            postOnConnected();
        }

        @Override
        public void onnErrorConnecting(Exception e) {

        }

        @Override
        public void onDisconnected() {
            releaseDecoder();
            postOnDisconnected();
        }

        @Override
        public void onError(Exception e) {
            postOnError(e);
        }

        @Override
        public void onStats(SRTStats stats) {
            postOnStats(stats);
        }
    };

    private final TsPacketExtractor.Callback mCallback = new TsPacketExtractor.Callback() {
        @Override
        public void onConfig(int pid, int streamType) {
            createDecoder(streamType);
            postOnReady();
        }

        @Override
        public void onByteStream(int pid, int streamId, byte[] data, int dataLength, long pts) {
            if (streamId == TsConstants.STREAM_ID_VIDEO) {
                if (mVideoDecoder != null) {
                    mVideoDecoder.onReceived(data, dataLength, pts);
                }
            } else if (streamId == TsConstants.STREAM_ID_AUDIO) {
                if (mAudioDecoder != null) {
                    mAudioDecoder.onReceived(data, dataLength, pts);
                }
            }
        }
    };

    public interface OnEventListener {
        /**
         * SRT サーバと接続したことを通知します.
         */
        void onConnected();

        /**
         * SRT サーバから切断したことを通知します.
         */
        void onDisconnected();

        /**
         * SRT サーバからの受信準備が完了したことを通知します.
         */
        void onReady();

        /**
         * 映像の解像度が変更されたことを通知します.
         *
         * @param width 横幅
         * @param height 縦幅
         */
        void onSizeChanged(int width, int height);

        /**
         * SRTPlayer でエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);

        /**
         * SRT サーバとの通信状況を通知します.
         *
         * @param stats 通信統計情報
         */
        void onStats(SRTStats stats);
    }
}
