package org.deviceconnect.android.libsrt.server;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.H264TransportStreamWriter;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;


public class Mpeg2TsMuxer extends SRTMuxer {

    private static final String TAG = "Mpeg2Ts";

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
    private H264TransportStreamWriter mH264TsSegmenter;

    /**
     * 映像と音声の両方を送信するフラグ.
     */
    private boolean mMixed;

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
     * SRT パケットのペイロード. SRT パケットのデフォルトの最大サイズに合わせる.
     */
    private final byte[] mPayload = new byte[188 * 7];

    /**
     * SRT パケットのペイロードを格納するためのバッファ.
     */
    private final ByteBuffer mPacketBuffer = ByteBuffer.wrap(mPayload);

    private final ByteArrayOutputStream mAudioRawCache = new ByteArrayOutputStream();

    byte[] getAudioRawCache() {
        return mAudioRawCache.toByteArray();
    }

    /**
     * mpeg2ts に変換されたデータを受信するリスナー.
     */
    private final H264TransportStreamWriter.PacketListener mBufferListener = (packet) -> {
        try {
            mPacketBuffer.put(packet);
            if (!mPacketBuffer.hasRemaining()) {
                sendPacket(mPayload);
                mPacketBuffer.clear();
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
        int fps = 30;

        if (videoQuality != null) {
            fps = videoQuality.getFrameRate();
        }

        if (audioQuality != null) {
            mProfile = 2;
            mChannelConfig = audioQuality.getChannelCount();
            mFreqIdx = getFreqIdx(audioQuality.getSamplingRate());
            sampleRate = audioQuality.getSamplingRate();
            sampleSizeInBits = audioQuality.getFormat();
            channels = audioQuality.getChannelCount();
        }

        mMixed = videoQuality != null && audioQuality != null;

        mH264TsSegmenter = new H264TransportStreamWriter();
        mH264TsSegmenter.setBufferListener(mBufferListener);
        mH264TsSegmenter.initialize(sampleRate, sampleSizeInBits, channels, fps);
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
                mH264TsSegmenter.pushVideoBuffer(mConfigBuffer, mMixed);
            }
            mH264TsSegmenter.pushVideoBuffer(encodedData, mMixed);
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        int outBitsSize = bufferInfo.size;
        int outPacketSize = outBitsSize + ADTS_LENGTH;
        if (mADTS == null || mADTS.length != outPacketSize) {
            mADTS = new byte[outPacketSize];
            mADTSBuffer = ByteBuffer.wrap(mADTS);
        }
        addADTStoPacket(mADTS, outPacketSize);
        encodedData.get(mADTS, ADTS_LENGTH, outBitsSize);

        if (DEBUG) {
            try {
                mAudioRawCache.write(mADTS);
            } catch (IOException ignored) {}
        }

        mADTSBuffer.limit(outPacketSize);
        mADTSBuffer.position(0);
        mH264TsSegmenter.pushAudioBuffer(mADTSBuffer, bufferInfo.size, mMixed);
    }

    @Override
    public void onReleased() {
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
