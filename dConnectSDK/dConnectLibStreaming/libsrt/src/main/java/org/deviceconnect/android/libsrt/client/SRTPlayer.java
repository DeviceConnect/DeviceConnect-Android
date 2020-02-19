package org.deviceconnect.android.libsrt.client;

import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsPacketExtractor;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.TsPacketReader;
import org.deviceconnect.android.libsrt.BuildConfig;
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
     * 描画先の Surface を設定します.
     *
     * @param surface 描画先の Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * SRT プレイヤーを開始します.
     *
     * @param address アドレス
     * @param port ポート番号
     */
    public void start(String address, int port) {
        if (mSRTClient != null) {
            return;
        }

        mVideoDecoder = new H264Decoder();
        mVideoDecoder.setSurface(mSurface);
        mVideoDecoder.setEventCallback(new VideoDecoder.EventCallback() {
            @Override
            public void onSizeChanged(int width, int height) {
                postOnSizeChanged(width, height);
            }
        });

        mPacketExtractor = new TsPacketExtractor();
        mPacketExtractor.setCallback(mCallback);
        mPacketExtractor.start();

        mSRTClient = new SRTClient(address, port);
        mSRTClient.setOnEventListener(mOnClientEventListener);
        mSRTClient.start();
    }

    /**
     * SRT プレイヤーを停止します.
     */
    public void stop() {
        if (mVideoDecoder != null) {
            mVideoDecoder.onReleased();
            mVideoDecoder = null;
        }

        if (mPacketExtractor != null) {
            mPacketExtractor.terminate();
            mPacketExtractor = null;
        }

        if (mSRTClient != null) {
            mSRTClient.stop();
            mSRTClient = null;
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

    private final SRTClient.OnEventListener mOnClientEventListener = new SRTClient.OnEventListener() {
        @Override
        public void onRead(byte[] data, int dataLength) {
            if (mPacketExtractor != null) {
                mPacketExtractor.add(data, dataLength);
            }
        }

        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected() {
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
