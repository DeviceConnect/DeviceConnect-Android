package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RTMP で指定された URL にデータを送信するためのマルチプレクサ.
 */
public class RtmpMuxer implements IMediaMuxer {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTMP-MUXER";

    /**
     * RMTP 用のマルチプレクサ.
     */
    private SrsFlvMuxer mSrsFlvMuxer;

    /**
     * 送信先の URL.
     */
    private String mUrl;

    /**
     * イベントを通知するためのリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * コンストラクタ.
     * @param url 送信先の URL
     */
    public RtmpMuxer(String url) {
        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }
        mUrl = url;
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
     * RTMPサーバに接続されているか確認します.
     *
     * @return RTMP サーバに接続されている場合はtrue、それ以外はfalse
     */
    private boolean isConnected() {
        return mSrsFlvMuxer != null && mSrsFlvMuxer.isConnected();
    }

    @Override
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        if (DEBUG) {
            Log.e(TAG, "RtmpMuxer::onPrepare");
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);

        ConnectCheckerRtmpAdapter rtmpAdapter = new ConnectCheckerRtmpAdapter() {
            @Override
            public void onConnectionSuccessRtmp() {
                super.onConnectionSuccessRtmp();
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onConnectionFailedRtmp(String reason) {
                super.onConnectionFailedRtmp(reason);
                result.set(false);
                latch.countDown();

                if (mOnEventListener != null) {
                    mOnEventListener.onError(new MediaEncoderException(reason));
                }
            }

            @Override
            public void onAuthErrorRtmp() {
                super.onAuthErrorRtmp();
                result.set(false);
                latch.countDown();
            }
        };

        mSrsFlvMuxer = new SrsFlvMuxer(rtmpAdapter);
        if (videoQuality != null) {
            mSrsFlvMuxer.setVideoResolution(videoQuality.getVideoWidth(), videoQuality.getVideoHeight());
        }
        if (audioQuality != null) {
            mSrsFlvMuxer.setSampleRate(audioQuality.getSamplingRate());
            mSrsFlvMuxer.setIsStereo(audioQuality.getChannelCount() == 2);
        }
        mSrsFlvMuxer.start(mUrl);

        // RTMPの配信準備が完了してから、エンコード処理を行わないと処理が進まないので、
        // ここで、配信準備が完了するのを待ちます。
        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore.
        }

        if (result.get() && mOnEventListener != null) {
            mOnEventListener.onConnected();
        }

        return result.get();
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
        mSrsFlvMuxer.setSpsPPs(newFormat.getByteBuffer("csd-0"), newFormat.getByteBuffer("csd-1"));
        try {
            int width = newFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = newFormat.getInteger(MediaFormat.KEY_HEIGHT);
            mSrsFlvMuxer.setVideoResolution(width, height);
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public synchronized void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (isConnected()) {
            mSrsFlvMuxer.sendVideo(encodedData, bufferInfo);
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public synchronized void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (isConnected()) {
            mSrsFlvMuxer.sendAudio(encodedData, bufferInfo);
        }
    }

    @Override
    public void onReleased() {
        if (mSrsFlvMuxer != null) {
            mSrsFlvMuxer.stop();
            mSrsFlvMuxer = null;
        }
    }

    private class ConnectCheckerRtmpAdapter implements ConnectCheckerRtmp {
        @Override
        public void onConnectionSuccessRtmp() {
            if (DEBUG) {
                Log.d(TAG, "RtmpMuxer::onConnectionSuccessRtmp");
            }
        }

        @Override
        public void onConnectionFailedRtmp(String reason) {
            if (DEBUG) {
                Log.e(TAG, "RtmpMuxer::onConnectionFailedRtmp");
                Log.e(TAG, "    reason: "+ reason);
            }
        }

        @Override
        public void onNewBitrateRtmp(long bitrate) {
            if (DEBUG) {
                Log.d(TAG, "RtmpMuxer::onNewBitrateRtmp");
                Log.d(TAG, "    bitrate: "+ bitrate);
            }

            if (mOnEventListener != null) {
                mOnEventListener.onNewBitrate(bitrate);
            }
        }

        @Override
        public void onDisconnectRtmp() {
            if (DEBUG) {
                Log.d(TAG, "RtmpMuxer::onDisconnectRtmp");
            }

            if (mOnEventListener != null) {
                mOnEventListener.onDisconnected();
            }
        }

        @Override
        public void onAuthErrorRtmp() {
            if (DEBUG) {
                Log.e(TAG, "RtmpMuxer::onAuthErrorRtmp");
            }
        }

        @Override
        public void onAuthSuccessRtmp() {
            if (DEBUG) {
                Log.d(TAG, "RtmpMuxer::onAuthSuccessRtmp");
            }
        }
    }

    /**
     * RtmpMuxer のイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * RTMP サーバに接続されたことを通知します.
         */
        void onConnected();

        /**
         * RTMP サーバから切断されたことを通知します.
         */
        void onDisconnected();

        /**
         * RTMP サーバからの通信ビットレートを通知します.
         *
         * @param bitrate ビットレート
         */
        void onNewBitrate(long bitrate);

        /**
         * RTMP サーバでエラーが発生したことを通知します.
         *
         * @param e エラー理由
         */
        void onError(MediaEncoderException e);
    }
}
