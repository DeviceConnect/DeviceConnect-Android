package org.deviceconnect.android.libmedia.streaming.rtsp.session;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.Connection;
import org.deviceconnect.android.libmedia.streaming.sdp.Information;
import org.deviceconnect.android.libmedia.streaming.sdp.Origin;
import org.deviceconnect.android.libmedia.streaming.sdp.SessionDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.SessionName;
import org.deviceconnect.android.libmedia.streaming.sdp.Time;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RtspSession {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-SESSION";

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

    /**
     * コンストラクタ.
     */
    public RtspSession() {
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

    /**
     * 各 MediaStream の設定情報を取得します.
     */
    public void configure() {
        for (MediaStream mediaStream : mStreamMap.values()) {
            mediaStream.configure();
        }
    }

    /**
     * RTSP セッションを開始します.
     */
    public void start() {
        mMediaStreamer.start();
    }

    /**
     * RTSP セッションを停止します.
     */
    public void stop() {
        mMediaStreamer.stop();
    }

    /**
     * セッションに登録されている MediaStream の SDP を作成します.
     *
     * @param remoteAddress 送信先のアドレス
     * @param localAddress 送信元のアドレス
     * @return SDP
     */
    public String createSessionDescription(String remoteAddress, String localAddress) {
        Origin origin = new Origin("-", 0L, 0L, "IN", "IP4", localAddress);
        SessionName sessionName = new SessionName("Unnamed");
        Information information = new Information("N/A");
        Connection connection = new Connection("IN", "IP4", remoteAddress);
        Time time = new Time(0, 0);
        Attribute attribute = new Attribute("recvonly");

        SessionDescription sdp = new SessionDescription();
        sdp.setOrigin(origin);
        sdp.setSessionName(sessionName);
        sdp.setInformation(information);
        sdp.setConnection(connection);
        sdp.addTime(time);
        sdp.addAttribute(attribute);

        for (MediaStream mediaStream : mStreamMap.values()) {
            sdp.addMediaDescriptions(mediaStream.getMediaDescription());
        }

        return sdp.toString();
    }

    /**
     * 登録されている MediaStream のコレクションを取得します.
     *
     * @return 登録されている MediaStream のコレクション
     */
    public Collection<MediaStream> getStreams() {
        return mStreamMap.values();
    }

    /**
     * 指定されたトラック ID の MediaStream を取得します.
     *
     * <p>
     * トラック ID に対応する MediaStream が存在しない場合は null を返却します.
     * </p>
     *
     * @param trackId トラックID
     * @return トラック ID に対応するMediaStream
     */
    public MediaStream getStream(String trackId) {
        return mStreamMap.get(trackId);
    }

    /**
     * 映像用の MediaStream を取得します.
     *
     * @return 映像用の MediaStream
     */
    public VideoStream getVideoStream() {
        return (VideoStream) mStreamMap.get(VIDEO_TRACK_ID);
    }

    /**
     * 音声用の MediaStream を取得します.
     *
     * @return 音声用の MediaStream
     */
    public AudioStream getAudioStream() {
        return (AudioStream) mStreamMap.get(AUDIO_TRACK_ID);
    }

    /**
     * 映像用の MediaStream を設定します.
     *
     * @param videoStream 映像用の MediaStream
     */
    public void setVideoMediaStream(VideoStream videoStream) {
        videoStream.setTrackId(VIDEO_TRACK_ID);
        mStreamMap.put(VIDEO_TRACK_ID, videoStream);
        mMediaStreamer.setVideoEncoder(videoStream.getVideoEncoder());
    }

    /**
     * 音声用の MediaStream を設定します.
     *
     * @param audioStream 音声用の MediaStream
     */
    public void setAudioMediaStream(AudioStream audioStream) {
        audioStream.setTrackId(AUDIO_TRACK_ID);
        mStreamMap.put(AUDIO_TRACK_ID, audioStream);
        mMediaStreamer.setAudioEncoder(audioStream.getAudioEncoder());
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
            if (DEBUG) {
                Log.i(TAG, "RtspSession::onVideoFormatChanged: " + newFormat);
            }
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
            if (DEBUG) {
                Log.i(TAG, "RtspSession::onAudioFormatChanged: " + newFormat);
            }
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
