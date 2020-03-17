package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
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

        return result.get();
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
        mSrsFlvMuxer.setSpsPPs(newFormat.getByteBuffer("csd-0"), newFormat.getByteBuffer("csd-1"));
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
        }

        @Override
        public void onDisconnectRtmp() {
            if (DEBUG) {
                Log.d(TAG, "RtmpMuxer::onDisconnectRtmp");
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
}
