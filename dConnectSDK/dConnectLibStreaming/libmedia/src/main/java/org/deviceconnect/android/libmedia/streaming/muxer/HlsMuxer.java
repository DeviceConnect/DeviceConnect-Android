package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;

import org.deviceconnect.android.ffmpeg.FFmpegEncoder;

import java.io.File;
import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class HlsMuxer implements IMediaMuxer {

    /**
     * 使用できるサンプリングレートを定義します.
     */
    private static final int[] SUPPORT_AUDIO_SAMPLING_RATES = {
            96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000,  // 11
            7350,  // 12
            -1,   // 13
            -1,   // 14
            -1,   // 15
    };

    /**
     * ADTS ヘッダーファイルサイズを定義します.
     */
    private static final int ADTS_LENGTH = 7;

    /**
     * データを一時的に格納するためのバッファ.
     */
    private byte[] mVideoConfig;

    /**
     * SPS、PPS のデータを格納するバッファ.
     */
    private ByteBuffer mVideoSPSandPPS;

    /**
     * m3u8 ファイル.
     */
    private File mM3U8;

    /**
     * ffmpeg で HLS にエンコードするためのクラス.
     */
    private FFmpegEncoder mFFmpegEncoder;

    /**
     * 音声データを一時的に格納するバッファ.
     */
    private byte[] mAudioPacket = new byte[1024];

    /**
     * 音声データを一時的に格納するバッファ.
     */
    private ByteBuffer mAudioBuffer = ByteBuffer.allocateDirect(1024);

    /**
     * ADTS ヘッダーに格納するサンプリングレートのインデックス.
     */
    private int mFreqIdx;

    /**
     * ADTS ヘッダーに格納するプロファイル.
     */
    private int mProfile;

    /**
     * ADTS ヘッダーに格納するチャンネル数.
     */
    private int mChannelConfig;

    /**
     * コンストラクタ.
     *
     * @param path HLS を出力するフォルダへのパス
     * @param m3u8 m3u8 のファイル名
     */
    public HlsMuxer(String path, String m3u8) {
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Failed to create a folder. path=" + path);
        }

        mM3U8 = new File(path, m3u8);
    }

    @Override
    public boolean onPrepare(VideoQuality quality, AudioQuality audioQuality) {
        if (audioQuality != null) {
            mProfile = 2;
            mChannelConfig = audioQuality.getChannelCount();
            mFreqIdx = getFreqIdx(audioQuality.getSamplingRate());
        }

        FFmpegEncoder.Setting setting = new FFmpegEncoder.Setting.Builder()
                .setType("hls")
                .setPath(mM3U8.getAbsolutePath())
                .setVideoWidth(quality.getVideoWidth())
                .setVideoHeight(quality.getVideoHeight())
                .setVideoBitRate(quality.getBitRate())
                .setHasAudio(audioQuality != null)
                .setAudioBitRate(audioQuality != null ? audioQuality.getBitRate() : 0)
                .setAudioSampleRate(audioQuality != null ? audioQuality.getSamplingRate() : 0)
                .setAudioChannel(audioQuality != null ? audioQuality.getChannelCount() : 0)
                .build();
        mFFmpegEncoder = new FFmpegEncoder(setting);
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public synchronized void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            createSPSandPPS(encodedData, bufferInfo);
            bufferInfo.size = 0;
        }

        if (bufferInfo.size != 0) {
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);

            if (checkKeyFrame(bufferInfo)) {
                packageSPSandPPS(encodedData, bufferInfo);
                mFFmpegEncoder.writePacket(mVideoSPSandPPS, true, bufferInfo.size + mVideoConfig.length, bufferInfo.presentationTimeUs);
            } else {
                mFFmpegEncoder.writePacket(encodedData, true, bufferInfo.size, bufferInfo.presentationTimeUs);
            }
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public synchronized void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (mFFmpegEncoder != null) {
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                bufferInfo.size = 0;
            }

            if (bufferInfo.size != 0) {
                int outBitsSize = bufferInfo.size;
                int outPacketSize = outBitsSize + ADTS_LENGTH;
                if (mAudioPacket.length < outPacketSize) {
                    mAudioPacket = new byte[outPacketSize];
                }
                addADTStoPacket(mAudioPacket, outPacketSize);
                encodedData.get(mAudioPacket, ADTS_LENGTH, outBitsSize);

                mAudioBuffer.clear();
                mAudioBuffer.put(mAudioPacket, 0, outPacketSize);
                mAudioBuffer.position(0);
                mAudioBuffer.limit(outPacketSize);

                mFFmpegEncoder.writePacket(mAudioBuffer, false, outPacketSize, bufferInfo.presentationTimeUs);
            }
        }
    }

    @Override
    public void onReleased() {
        if (mFFmpegEncoder != null) {
            mFFmpegEncoder.release();
            mFFmpegEncoder = null;
        }
    }

    /**
     * 指定されたコーデックのバッファ情報がキーフレームか確認します.
     *
     * @param bufferInfo バッファ情報
     * @return キーフレームの場合はtrue、それ以外はfalse
     */
    private boolean checkKeyFrame(MediaCodec.BufferInfo bufferInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
        } else {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        }
    }

    private void createSPSandPPS(final ByteBuffer encodedData, final MediaCodec.BufferInfo bufferInfo) {
        mVideoSPSandPPS = ByteBuffer.allocateDirect(bufferInfo.size);
        mVideoConfig = new byte[bufferInfo.size];
        encodedData.get(mVideoConfig, 0, bufferInfo.size);
        encodedData.position(bufferInfo.offset);
        mVideoSPSandPPS.put(mVideoConfig, 0, bufferInfo.size);
    }

    private void packageSPSandPPS(final ByteBuffer encodedData, final MediaCodec.BufferInfo bufferInfo) {
        if (mVideoSPSandPPS.capacity() < mVideoConfig.length + bufferInfo.size) {
            mVideoSPSandPPS = ByteBuffer.allocateDirect(mVideoConfig.length + bufferInfo.size);
            mVideoSPSandPPS.put(mVideoConfig);
            mVideoSPSandPPS.put(encodedData);
        } else {
            mVideoSPSandPPS.position(mVideoConfig.length);
            mVideoSPSandPPS.put(encodedData);
        }
        mVideoSPSandPPS.position(0);
    }

    private int getFreqIdx(int sampleRate) {
        for (int i = 0; i < SUPPORT_AUDIO_SAMPLING_RATES.length; i++) {
            if (sampleRate == SUPPORT_AUDIO_SAMPLING_RATES[i]) {
                return i;
            }
        }
        return -1;
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((mProfile - 1) << 6) + (mFreqIdx << 2) + (mChannelConfig >> 2));
        packet[3] = (byte) (((mChannelConfig & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
