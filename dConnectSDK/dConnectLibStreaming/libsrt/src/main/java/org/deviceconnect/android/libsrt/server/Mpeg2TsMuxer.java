package org.deviceconnect.android.libsrt.server;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.H264TransportStreamWriter;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;

public class Mpeg2TsMuxer extends SRTMuxer {
    /**
     * SPS と PPS のデータを格納するバッファ.
     */
    private ByteBuffer mConfigBuffer;

    /**
     * H264 のセグメントに分割するクラス.
     */
    private H264TransportStreamWriter mH264TsSegmenter;

    /**
     * mpeg2ts に変換されたデータを受信するリスナー.
     */
    private final H264TransportStreamWriter.PacketListener mBufferListener = (result) -> {
        try {
            final int max = 188 * 7;
            if (result.length > max) {
                for (int offset = 0; offset < result.length; offset += max) {
                    final int length;
                    if (result.length - offset < max) {
                        length = result.length - offset;
                    } else {
                        length = max;
                    }
                    sendPacket(result, offset, length);
                }
            } else {
                sendPacket(result);
            }
        } catch (Exception e) {
            Log.e("Mpeg2Ts", "Failed to send packet", e);
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
            sampleRate = audioQuality.getSamplingRate();
            sampleSizeInBits = audioQuality.getFormat();
            channels = audioQuality.getChannelCount();
        }

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
                mH264TsSegmenter.generatePackets(mConfigBuffer);
            }
            mH264TsSegmenter.generatePackets(encodedData);
        }

    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        // TODO 音声データも H264TsSegmenter に渡して良いか確認すること。
    }

    @Override
    public void onReleased() {
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
