package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;

public class AACH264TsPacketWriter {

    public interface PacketListener {
        void onPacketAvailable(byte[] packet);
    }

    private static final int H264NT_SLICE  = 1;

    private static final int H264NT_SLICE_IDR = 5;

    private static final byte[] H264_START_CODE = {0x00, 0x00, 0x00, 0x01};

    private final TsPacketWriter mTsWriter;

    private boolean mFirstPes = true;

    private PacketListener mPacketListener;

    private int mFps;
    private float mSampleRate;
    private int mSampleSizeInBits;
    private int mChannel;
    private long mIncVideoPts;
    private long mIncAudioPts;
    private boolean mMixed;

    public AACH264TsPacketWriter() {
        mTsWriter = new TsPacketWriter();
        mTsWriter.setCallback(packet -> mPacketListener.onPacketAvailable(packet));
    }

    public void setPacketListener(PacketListener packetListener) {
        this.mPacketListener = packetListener;
    }

    public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
        mSampleRate = sampleRate;
        mSampleSizeInBits = sampleSizeInBits;
        mChannel = channels;
        mFps = fps;

        if (fps > 0) {
            // PTS ステップサイズ.
            // ・映像：1000 / fps（単位：ms）
            // ・ミリ秒への変換：h264の設定によると90HZであるため、PTS/DTSのミリ秒への変換式は次のとおり。ms = pts / 90
            mIncVideoPts = (long) (1000 / fps) * 90;
        }

        if (sampleRate > 0) {
            // ・音声: 1つのAACフレームに対応するサンプリングサンプル数/サンプリング周波数（単位: s)
            mIncAudioPts = (long) (1024 * (1000 / sampleRate)) * 90;
        }

        mMixed = (fps > 0 && sampleRate > 0);

        // 初期化フラグ
        mFirstPes = true;
    }

    public synchronized void writeNALU(final ByteBuffer buffer, final long pts) {
        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = buffer.get(H264_START_CODE.length) & 0x1F;
        boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
        buffer.position(offset);
        mTsWriter.writeVideoBuffer(mFirstPes, buffer, length, pts, pts, isFrame, mMixed);
    }

    public synchronized void writeADTS(final ByteBuffer buffer, long pts) {
        int length = buffer.limit() - buffer.position();
        mTsWriter.writeAudioBuffer(mFirstPes, buffer, length, pts, pts, mMixed);
        mFirstPes = false;
    }
}
