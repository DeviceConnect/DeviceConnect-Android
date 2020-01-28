package org.deviceconnect.android.libsrt.server;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.H264TsSegmenter;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;

import static org.deviceconnect.android.libsrt.BuildConfig.DEBUG;

public class Mpeg2TsMuxer extends SRTMuxer {

    /**
     * 音声用のデータを一時的に格納するバッファ.
     */
    private byte[] mAudioBuffer = new byte[4096];

    /**
     * H264 のセグメントに分割するkクラス.
     */
    private H264TsSegmenter mH264TsSegmenter;

    /**
     * データを一時的に格納するバッファ.
     */
    private ByteBuffer mByteBuffer = ByteBuffer.allocate(4096);

    /**
     * mpeg2ts に変換されたデータを受信するリスナー.
     */
    private final H264TsSegmenter.BufferListener mBufferListener = (result) -> {
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

        mH264TsSegmenter = new H264TsSegmenter();
        mH264TsSegmenter.setBufferListener(mBufferListener);
        mH264TsSegmenter.initialize(sampleRate, sampleSizeInBits, channels, fps);
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        // PTS[k]
        //   = ((system_clock_frequency * presentation_time_in_seconds) / 300) % (2^33)
        //   = (((27 * 1000 * 1000) * presentation_time_in_milliseconds / 1000) / 30) % (2^33)
        //   = (presentation_time_in_milliseconds * 90) % (2^33)
        long pts = (((System.currentTimeMillis()) * 90) % 8589934592L);
        
        mH264TsSegmenter.generatePackets(encodedData, pts);
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (mAudioBuffer.length < bufferInfo.size) {
            mAudioBuffer = new byte[bufferInfo.size];
        }
        encodedData.get(mAudioBuffer, 0, bufferInfo.size);

        // TODO 音声データも H264TsSegmenter に渡して良いか確認すること。

        writePacket(mAudioBuffer, bufferInfo.size, bufferInfo.presentationTimeUs);
    }

    @Override
    public void onReleased() {
    }

    /**
     * 送信するパケットデータを書き込みます.
     *
     * @param packet パケットデータ
     * @param packetLength パケットデータサイズ
     * @param pts プレゼンテションタイム
     */
    private void writePacket(byte[] packet, int packetLength, long pts) {
        if (mByteBuffer.capacity() < packetLength) {
            mByteBuffer = ByteBuffer.allocate(packetLength);
        }
        mByteBuffer.clear();
        mByteBuffer.put(packet, 0, packetLength);
        mByteBuffer.flip();

        mH264TsSegmenter.generatePackets(mByteBuffer, pts / 1000L);
    }
}
