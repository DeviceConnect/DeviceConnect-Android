package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.List;


/**
 * H.264 形式の映像配信用 Transport Stream を生成するクラス.
 */
public class H264TransportStreamWriter {

    public interface PacketListener {
        void onPacketAvailable(byte[] packet);
    }

    private static final int H264NT_SLICE  = 1;

    private static final int H264NT_SLICE_IDR = 5;

    private static final byte[] H264_START_CODE = {0x00, 0x00, 0x00, 0x01};

    private final H264TransportPacketWriter tsWriter;

    private boolean isFirstPes = true;

    private PacketListener bufferListener;

    private int fps;

    private PTS videoPts;

    private PTS audioPts;

    public H264TransportStreamWriter() {
        tsWriter = new H264TransportPacketWriter();
        tsWriter.setCallback(packet -> bufferListener.onPacketAvailable(packet));
    }

    public void setBufferListener(final PacketListener bufferListener) {
        this.bufferListener = bufferListener;
    }

    public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
        this.fps = fps;

        /*
         * ptsステップサイズ.
         *
         * ・音声: 1つのAACフレームに対応するサンプリングサンプル数/サンプリング周波数（単位: s)
         * ・映像：1000 / fps（単位：ms）
         * ・ミリ秒への変換：h264の設定によると90HZであるため、PTS/DTSのミリ秒への変換式は次のとおり。ms = pts / 90
         */
        this.videoPts = new PTS((long) (1000 / this.fps) * 90);
        if (sampleRate > 0) {
            this.audioPts = new PTS((90000 << 10) / (int) sampleRate);
        }
    }

    public synchronized void pushVideoBuffer(final ByteBuffer buffer, final boolean mixed) {
        List<Integer> offsets = ByteUtil.kmp(buffer, H264_START_CODE);
        buffer.position(0);
        int totalLength = buffer.remaining();
        for (int i = 0; i < offsets.size(); i++) {
            final int unitOffset = offsets.get(i);
            final int unitLength;
            if (i == offsets.size() - 1) {
                unitLength = totalLength - unitOffset;
            } else {
                int nextOffset = offsets.get(i + 1);
                unitLength = nextOffset - unitOffset;
            }
            int type = buffer.get(unitOffset + H264_START_CODE.length) & 0x1F;

            boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
            long pts = videoPts.getPts();
            buffer.position(unitOffset);
            tsWriter.writeVideoBuffer(isFirstPes, buffer, unitLength, pts, pts, isFrame, mixed);
            isFirstPes = false;
        }
    }

    public synchronized void pushAudioBuffer(final ByteBuffer buffer, final int length, final boolean mixed) {
        long pts = audioPts.getPts();
        tsWriter.writeAudioBuffer(isFirstPes, buffer, length, pts, pts, mixed);
        isFirstPes = false;
    }

    private static class PTS {
        long time;
        final long inc;

        PTS(long inc) {
            this.inc = inc;
        }

        long getPts() {
            return time += inc;
        }
    }
}
