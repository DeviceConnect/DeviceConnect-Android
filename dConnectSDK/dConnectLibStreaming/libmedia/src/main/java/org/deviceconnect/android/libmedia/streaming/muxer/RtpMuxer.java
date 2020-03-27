package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpSocket;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public abstract class RtpMuxer implements IMediaMuxer {
    /**
     * 送信先のアドレス.
     */
    private InetAddress mRemoteAddress;

    /**
     * 映像用の RTP を配信するためのポート番号.
     */
    private int mVideoPort;

    /**
     * 映像用の RTCP を配信するためのポート番号.
     */
    private int mVideoRtcp;

    /**
     * 音声用の RTP を配信するためのポート番号.
     */
    private int mAudioPort;

    /**
     * 音声用の RTCP を配信するためのポート番号.
     */
    private int mAudioRtcp;

    /**
     * 映像用の RTP、RTCP を送信するためのソケット.
     */
    private RtpSocket mVideoSocket;

    /**
     * 音声用の RTP、RTCP を送信するためのソケット.
     */
    private RtpSocket mAudioSocket;

    /**
     * 映像を RTP のパケットに変換するためのクラス.
     */
    private RtpPacketize mVideoPacketize;

    /**
     * 音声を RTP のパケットに変換するためのクラス.
     */
    private RtpPacketize mAudioPacketize;

    /**
     * 映像用のデータを一時的に格納するバッファ.
     */
    private byte[] mVideoBuffer = new byte[4096];

    /**
     * 音声用のデータを一時的に格納するバッファ.
     */
    private byte[] mAudioBuffer = new byte[4096];

    /**
     * PPS のデータを格納するためのバッファ.
     */
    private byte[] mPPS;

    /**
     * SPS のデータを格納するためのバッファ.
     */
    private byte[] mSPS;

    /**
     * コンストラクタ.
     *
     * @param remoteAddress 送信先のアドレス
     * @param videoPort 映像用のポート番号
     * @param audioPort 音声用のポート番号
     */
    public RtpMuxer(InetAddress remoteAddress, int videoPort, int audioPort) {
        this(remoteAddress, videoPort, videoPort + 1, audioPort, audioPort + 1);
    }

    /**
     * コンストラクタ.
     *
     * @param remoteAddress 送信先のアドレス
     * @param videoPort 映像用のポート番号
     * @param audioPort 音声用のポート番号
     */
    public RtpMuxer(InetAddress remoteAddress, int videoPort, int videoRtcp, int audioPort, int audioRtcp) {
        mRemoteAddress = remoteAddress;
        mVideoPort = videoPort;
        mVideoRtcp = videoRtcp;
        mAudioPort = audioPort;
        mAudioRtcp = audioRtcp;
    }

    /**
     * コンストラクタ.
     *
     * @param remoteAddress 送信先のアドレス
     * @param videoPort 映像用のポート番号
     * @param audioPort 音声用のポート番号
     */
    public RtpMuxer(String remoteAddress, int videoPort, int audioPort) throws UnknownHostException {
        this(InetAddress.getByName(remoteAddress), videoPort, audioPort);
    }

    /**
     * コンストラクタ.
     *
     * @param remoteAddress 送信先のアドレス
     * @param videoPort 映像用のポート番号
     * @param audioPort 音声用のポート番号
     */
    public RtpMuxer(String remoteAddress, int videoPort, int videoRtcp, int audioPort, int audioRtcp) throws UnknownHostException {
        this(InetAddress.getByName(remoteAddress), videoPort, videoRtcp, audioPort, audioRtcp);
    }

    /**
     * 映像用の RTP パケット作成クラスを取得します.
     *
     * @param videoQuality 映像設定
     * @return RtpPacketizeの実装クラス
     */
    protected abstract RtpPacketize createVideoPacketize(VideoQuality videoQuality);

    /**
     * 音声用の RTP パケット作成クラスを取得します.
     *
     * @param audioQuality 映像設定
     * @return RtpPacketizeの実装クラス
     */
    protected abstract RtpPacketize createAudioPacketize(AudioQuality audioQuality);

    @Override
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        if (videoQuality != null) {
            try {
                mVideoSocket = new RtpSocket();
                mVideoSocket.setDestination(mRemoteAddress, mVideoPort, mVideoRtcp);
                mVideoSocket.open();

                mVideoPacketize = createVideoPacketize(videoQuality);
                if (mVideoPacketize != null) {
                    mVideoPacketize.setSsrc(mVideoSocket.getSsrc());
                    mVideoPacketize.setCallback(mVideoSocket);
                }
            } catch (IOException e) {
                // ignore.
            }
        }
        if (audioQuality != null) {
            try {
                mAudioSocket = new RtpSocket();
                mAudioSocket.setDestination(mRemoteAddress, mAudioPort, mAudioRtcp);
                mAudioSocket.open();

                mAudioPacketize = createAudioPacketize(audioQuality);
                if (mAudioPacketize != null) {
                    mAudioPacketize.setSsrc(mAudioSocket.getSsrc());
                    mAudioPacketize.setCallback(mAudioSocket);
                }
            } catch (IOException e) {
                // ignore.
            }
        }
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (mVideoPacketize != null) {
            boolean isConfigFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
            if (isConfigFrame) {
                byte[] configData = new byte[bufferInfo.size];
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);
                encodedData.get(configData, 0, bufferInfo.size);
                int configLength = bufferInfo.size;

                search(configData, configLength, (data, startPos, length) -> {
                    switch (data[startPos + 4] & 0x1F) {
                        case 0x07:
                            mSPS = new byte[length];
                            System.arraycopy(data, startPos, mSPS, 0, length);
                            break;
                        case 0x08:
                            mPPS = new byte[length];
                            System.arraycopy(data, startPos, mPPS, 0, length);
                            break;
                        default:
                            break;
                    }
                });
            }

            boolean isKeyFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
            if (isKeyFrame && mSPS != null && mPPS != null) {
                // H264 の SPS、PPS はキーフレームごとに送信するようにする。
                mVideoPacketize.write(mSPS, mSPS.length, bufferInfo.presentationTimeUs);
                mVideoPacketize.write(mPPS, mPPS.length, bufferInfo.presentationTimeUs);
            }

            if (mVideoBuffer.length < bufferInfo.size) {
                mVideoBuffer = new byte[bufferInfo.size];
            }
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            encodedData.get(mVideoBuffer, 0, bufferInfo.size);

            mVideoPacketize.write(mVideoBuffer, bufferInfo.size, bufferInfo.presentationTimeUs);
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (mAudioPacketize != null) {
            if (mAudioBuffer.length < bufferInfo.size) {
                mAudioBuffer = new byte[bufferInfo.size];
            }
            encodedData.get(mAudioBuffer, 0, bufferInfo.size);

            mAudioPacketize.write(mAudioBuffer, bufferInfo.size, bufferInfo.presentationTimeUs);
        }
    }

    @Override
    public void onReleased() {
        if (mVideoSocket != null) {
            mVideoSocket.close();
            mVideoSocket = null;
        }

        if (mAudioSocket != null) {
            mAudioSocket.close();
            mAudioSocket = null;
        }
    }

    private static void search(byte[] data, int dataLength, Callback callback) {
        int startPos = 0;
        int count = 0;
        for (int i = 0; i < dataLength - 4; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                if (count > 0) {
                    callback.onData(data, startPos, (i - startPos));
                }
                startPos = i;
                count++;
            }
        }

        if (dataLength - startPos > 0) {
            callback.onData(data, startPos, (dataLength - startPos));
        }
    }

    private interface Callback {
        void onData(byte[] data, int startPos, int length);
    }
}
