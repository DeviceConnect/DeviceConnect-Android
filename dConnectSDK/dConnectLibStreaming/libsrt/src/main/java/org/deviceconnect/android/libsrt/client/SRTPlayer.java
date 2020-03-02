package org.deviceconnect.android.libsrt.client;

import android.net.Uri;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsPacketExtractor;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsPacketReader;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.client.decoder.audio.AACDecoder;
import org.deviceconnect.android.libsrt.client.decoder.audio.AudioDecoder;
import org.deviceconnect.android.libsrt.client.decoder.video.H264Decoder;
import org.deviceconnect.android.libsrt.client.decoder.video.VideoDecoder;

import java.io.IOException;

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
    private static final long DEFAULT_STATS_INTERVAL = SRTClient.DEFAULT_STATS_INTERVAL;

    /**
     * 動画のストリームタイプを定義.
     */
    private static final int STREAM_TYPE_VIDEO = 0xE0;

    /**
     * 音声のストリームタイプを定義.
     */
    private static final int STREAM_TYPE_AUDIO = 0xC0;

    /**
     * SRT の通信を行うクラス.
     */
    private SRTClient mSRTClient;

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
     * SRT プレイヤーを開始します.
     */
    public void start() {
        if (mSRTClient != null) {
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

        mSRTClient = new SRTClient(uri.getHost(), uri.getPort());
        mSRTClient.setOnEventListener(mOnClientEventListener);
        mSRTClient.setStatsInterval(mStatsInterval);
        mSRTClient.setShowStats(mShowStats);
        mSRTClient.start();
    }

    /**
     * SRT プレイヤーを停止します.
     */
    public void stop() {
        if (mPacketExtractor != null) {
            mPacketExtractor.terminate();
            mPacketExtractor = null;
        }

        if (mSRTClient != null) {
            mSRTClient.stop();
            mSRTClient = null;
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
        if (mSRTClient != null) {
            mSRTClient.setStatsInterval(mStatsInterval);
            mSRTClient.setShowStats(showStats);
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

    private final SRTClient.OnEventListener mOnClientEventListener = new SRTClient.OnEventListener() {
        @Override
        public void onReceived(byte[] data, int dataLength) {
            if (mPacketExtractor != null) {
                mPacketExtractor.add(data, dataLength);
            }
        }

        @Override
        public void onConnected() {
            mVideoDecoder = new H264Decoder();
            mVideoDecoder.setSurface(mSurface);
            mVideoDecoder.setErrorCallback(SRTPlayer.this::postOnError);
            mVideoDecoder.setEventCallback(SRTPlayer.this::postOnSizeChanged);
            mVideoDecoder.onInit();

            mAudioDecoder = new AACDecoder();
            mAudioDecoder.setErrorCallback(SRTPlayer.this::postOnError);
            mAudioDecoder.onInit();
        }

        @Override
        public void onDisconnected() {
            if (mVideoDecoder != null) {
                mVideoDecoder.onReleased();
                mVideoDecoder = null;
            }

            if (mAudioDecoder != null) {
                mAudioDecoder.onReleased();
                mAudioDecoder = null;
            }
        }

        @Override
        public void onError(IOException e) {
            postOnError(e);
        }
    };

    private final TsPacketReader.Callback mCallback = (streamId, data, dataLength, pts) -> {
        if (streamId == STREAM_TYPE_VIDEO) {
            if (mVideoDecoder != null) {
                mVideoDecoder.onReceived(data, dataLength, pts);
            }
        } else if (streamId == STREAM_TYPE_AUDIO) {
            if (mAudioDecoder != null) {
                mAudioDecoder.onReceived(data, dataLength, pts);
            }
        }
    };

    public interface OnEventListener {
        void onSizeChanged(int width, int height);
        void onError(Exception e);
    }
}
