package org.deviceconnect.android.libsrt.server;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.AacH264TsPacketWriter;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.AacH265TsPacketWriter;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.AacH26xTsPacketWriter;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;


public class Mpeg2TsMuxer extends SRTMuxer {

    private static final String TAG = "Mpeg2Ts";

    private static final int TS_PACKET_SIZE = 188;
    private static final int PAYLOAD_SIZE = TS_PACKET_SIZE * 7;

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
     * SPS と PPS のデータを格納するバッファ.
     */
    private ByteBuffer mConfigBuffer;

    /**
     * H264 のセグメントに分割するクラス.
     */
    private AacH26xTsPacketWriter mTsWriter;

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
     * ADTS を保持するためのバイト配列.
     */
    private byte[] mADTS = null;

    /**
     * ADTS の バイト配列をラップしたバッファ.
     */
    private ByteBuffer mADTSBuffer = null;

    /**
     * SRT パケットのペイロード.
     * <p>
     * SRT パケットのデフォルトの最大サイズに合わせる.
     * </p>
     */
    private final byte[] mPayload = new byte[PAYLOAD_SIZE];

    /**
     * SRT パケットのペイロード位置.
     */
    private int mPayloadPosition;

    /**
     * エンコードを開始したプレゼンテーションタイム.
     */
    private long mPresentationTime;

    /**
     * TS パケットに変換されたデータを受信するリスナー.
     */
    private final AacH26xTsPacketWriter.PacketListener mPacketListener = (packet) -> {
        try {
            if (packet == null) {
                if (mPayloadPosition > 0) {
                    sendPacket(mPayload, 0, mPayloadPosition);
                }
                mPayloadPosition = 0;
            } else {
                System.arraycopy(packet, 0, mPayload, mPayloadPosition, TS_PACKET_SIZE);
                mPayloadPosition += TS_PACKET_SIZE;

                if (mPayloadPosition == PAYLOAD_SIZE) {
                    mPayloadPosition = 0;
                    sendPacket(mPayload);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to send packet", e);
        }
    };

    @Override
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        float sampleRate = 0;
        int sampleSizeInBits = 0;
        int channels = 0;
        int fps = 0;
        boolean h265 = false;

        if (videoQuality != null) {
            fps = videoQuality.getFrameRate();
            h265 = "video/hevc".equalsIgnoreCase(videoQuality.getMimeType());
        }

        if (audioQuality != null) {
            mProfile = 2;
            mChannelConfig = audioQuality.getChannelCount();
            mFreqIdx = getFreqIdx(audioQuality.getSamplingRate());
            sampleRate = audioQuality.getSamplingRate();
            sampleSizeInBits = audioQuality.getFormat();
            channels = audioQuality.getChannelCount();
        }

        if (h265) {
            mTsWriter = new AacH265TsPacketWriter();
        } else {
            mTsWriter = new AacH264TsPacketWriter();
        }
        mTsWriter.initialize(sampleRate, sampleSizeInBits, channels, fps);
        mTsWriter.setPacketListener(mPacketListener);
        mPresentationTime = 0;
        mPayloadPosition = 0;
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        encodedData.position(bufferInfo.offset);
        encodedData.limit(bufferInfo.offset + bufferInfo.size);

        if (isConfigFrame(bufferInfo)) {
            storeConfig(encodedData, bufferInfo);
        } else {
            if (isKeyFrame(bufferInfo) && mConfigBuffer.limit() > 0) {
                mConfigBuffer.position(0);
                mTsWriter.writeNALU(mConfigBuffer, getPts(bufferInfo));
            }
            mTsWriter.writeNALU(encodedData, getPts(bufferInfo));
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (isConfigFrame(bufferInfo)) {
            return;
        }

        int outPacketSize = bufferInfo.size;
        if (isADTSPacket(encodedData)) {
            // 送られてきたデータが ADTS ヘッダーを含んでいる場合
            checkADTSBuffer(outPacketSize);
            encodedData.get(mADTS, 0, outPacketSize);
        } else {
            outPacketSize += ADTS_LENGTH;
            checkADTSBuffer(outPacketSize);

            addADTStoPacket(mADTS, outPacketSize);
            encodedData.get(mADTS, ADTS_LENGTH, bufferInfo.size);
        }

        mADTSBuffer.position(0);
        mADTSBuffer.limit(outPacketSize);
        mTsWriter.writeADTS(mADTSBuffer, getPts(bufferInfo));
    }

    @Override
    public void onReleased() {
    }

    /**
     * MPEG-TS 用の PTS に変換します.
     *
     * @param bufferInfo MediaCodec のバッファ情報
     * @return PTS
     */
    private long getPts(MediaCodec.BufferInfo bufferInfo) {
        if (mPresentationTime == 0) {
            mPresentationTime = bufferInfo.presentationTimeUs;
        }
        return ((bufferInfo.presentationTimeUs - mPresentationTime) / 1000) * 90;
    }

    /**
     * ADTS に指定するサンプルレートに対応するインデックスを取得します.
     *
     * @param sampleRate サンプルレート
     * @return サンプルレートに対応するインデックス
     */
    private int getFreqIdx(int sampleRate) {
        for (int i = 0; i < SUPPORT_AUDIO_SAMPLING_RATES.length; i++) {
            if (sampleRate == SUPPORT_AUDIO_SAMPLING_RATES[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * バッファサイズを確認し、パケットサイズよりも小さい場合には拡張します.
     *
     * @param outPacketSize パケットサイズ
     */
    private void checkADTSBuffer(int outPacketSize) {
        if (mADTS == null || mADTS.length < outPacketSize) {
            mADTS = new byte[outPacketSize];
            mADTSBuffer = ByteBuffer.wrap(mADTS);
        }
    }

    /**
     * 指定されたデータの先頭が 0xFFF0 になっている確認します.
     *
     * @param encodedData データ
     * @return 0xFFF0 で開始されている場合は true、それ以外はfalse.
     */
    private boolean isADTSPacket(ByteBuffer encodedData) {
        return (encodedData.get(0) & 0xFF) == 0xFF && (encodedData.get(1) & 0xF0) == 0xF0;
    }

    /**
     * ADTS ヘッダーを追加します.
     *
     * @param packet パケット
     * @param packetLen パケットサイズ
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((mProfile - 1) << 6) + (mFreqIdx << 2) + (mChannelConfig >> 2));
        packet[3] = (byte) (((mChannelConfig & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * SPS、PPS などの設定値か確認します.
     *
     * @param bufferInfo 映像データの情報
     * @return 設定データの場合はtrue、それ以外はfalse
     */
    private boolean isConfigFrame(MediaCodec.BufferInfo bufferInfo) {
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
    }

    /**
     * キーフレームか確認します.
     *
     * @param bufferInfo 映像データの情報
     * @return キーフレームの場合はtrue、それ以外はfalse
     */
    @SuppressWarnings("deprecation")
    private boolean isKeyFrame(MediaCodec.BufferInfo bufferInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
        } else {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        }
    }

    /**
     * SPS、PPS の設定値を一時保管します.
     *
     * @param encodedData 映像データ
     * @param bufferInfo 映像データの情報
     */
    private void storeConfig(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        mConfigBuffer = ByteBuffer.allocateDirect(bufferInfo.size);
        byte[] data = new byte[bufferInfo.size];
        encodedData.get(data);
        mConfigBuffer.put(data);
        mConfigBuffer.flip();
    }
}
