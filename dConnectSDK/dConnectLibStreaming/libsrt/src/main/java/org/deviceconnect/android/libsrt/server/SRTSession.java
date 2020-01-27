package org.deviceconnect.android.libsrt.server;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.server.audio.AudioStream;
import org.deviceconnect.android.libsrt.server.video.VideoStream;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * 音声用のトラック ID を定義します.
     */
    private static final String AUDIO_TRACK_ID = "0";

    /**
     * 映像用のトラック ID を定義します.
     */
    private static final String VIDEO_TRACK_ID = "1";

    /**
     * ストリーミングを行うためのクラス.
     */
    private MediaStreamer mMediaStreamer;

    /**
     * MediaStream を格納するための Map.
     */
    private Map<String, MediaStream> mStreamMap = new LinkedHashMap<>();


    SRTSession() {
        mMediaStreamer = new MediaStreamer(mMediaMuxer);
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

    public void configure() {
        for (MediaStream mediaStream : mStreamMap.values()) {
            mediaStream.configure();
        }
    }

    public void start() {
        mMediaStreamer.start();
    }

    public void stop() {
        mMediaStreamer.stop();
    }

    public void setVideoStream(VideoStream videoStream) {
        mStreamMap.put(VIDEO_TRACK_ID, videoStream);
        mMediaStreamer.setVideoEncoder(videoStream.getVideoEncoder());
    }

    public void setAudioStream(AudioStream audioStream) {
        mStreamMap.put(AUDIO_TRACK_ID, audioStream);
        mMediaStreamer.setAudioEncoder(audioStream.getAudioEncoder());
    }

    public VideoStream getVideoStream() {
        return (VideoStream) mStreamMap.get(VIDEO_TRACK_ID);
    }

    public AudioStream getAudioStream() {
        return (AudioStream) mStreamMap.get(AUDIO_TRACK_ID);
    }

    /**
     * エンコードされたデータを送信する処理を行います.
     */
    private final IMediaMuxer mMediaMuxer = new IMediaMuxer() {
        /**
         * 映像用のデータを一時的に格納するバッファ.
         */
        private byte[] mVideoBuffer = new byte[4096];

        /**
         * 音声用のデータを一時的に格納するバッファ.
         */
        private byte[] mAudioBuffer = new byte[4096];

        /**
         * PPS、SPS のデータを一時的に格納するバッファ.
         */
        private byte[] mConfigData;

        /**
         * PPS、SPS のデータをバッファサイズ.
         */
        private int mConfigLength;

        @Override
        public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
            return true;
        }

        @Override
        public void onVideoFormatChanged(MediaFormat newFormat) {
        }

        @Override
        public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
            VideoStream videoStream = getVideoStream();
            if (videoStream != null) {
                boolean isConfigFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
                if (isConfigFrame) {
                    if (mConfigData == null || mConfigData.length < bufferInfo.size) {
                        mConfigData = new byte[bufferInfo.size];
                    }
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    encodedData.get(mConfigData, 0, bufferInfo.size);
                    mConfigLength = bufferInfo.size;
                }

                boolean isKeyFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                if (isKeyFrame && mConfigData != null) {
                    // H264 の SPS、PPS はキーフレームごとに送信するようにする。
                    videoStream.writePacket(mConfigData, mConfigLength, bufferInfo.presentationTimeUs);
                }

                if (mVideoBuffer.length < bufferInfo.size) {
                    mVideoBuffer = new byte[bufferInfo.size];
                }
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);
                encodedData.get(mVideoBuffer, 0, bufferInfo.size);

                videoStream.writePacket(mVideoBuffer, bufferInfo.size, bufferInfo.presentationTimeUs);
            }
        }

        @Override
        public void onAudioFormatChanged(MediaFormat newFormat) {
        }

        @Override
        public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
            AudioStream audioStream = getAudioStream();
            if (audioStream != null) {
                if (mAudioBuffer.length < bufferInfo.size) {
                    mAudioBuffer = new byte[bufferInfo.size];
                }
                encodedData.get(mAudioBuffer, 0, bufferInfo.size);

                audioStream.writePacket(mAudioBuffer, bufferInfo.size, bufferInfo.presentationTimeUs);
            }
        }

        @Override
        public void onReleased() {
            for (MediaStream mediaStream : mStreamMap.values()) {
                mediaStream.release();
            }
        }
    };
}
