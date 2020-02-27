package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;

public class AacH264TsPacketWriter extends AacH26xTsPacketWriter {
    /**
     * P フレーム（差分)のタイプを定義.
     */
    private static final int H264NT_SLICE  = 1;

    /**
     * I フレームのタイプを定義.
     */
    private static final int H264NT_SLICE_IDR = 5;

    @Override
    public synchronized void writeNALU(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.VIDEO);

        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = buffer.get(4) & 0x1F;
        boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
        buffer.position(offset);
        mTsWriter.writeVideoBuffer(mFirstPes, put(buffer, length), length, getPcr(), pts, 0, isFrame, mMixed);
        mFirstPes = false;
    }

    @Override
    public synchronized void writeADTS(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.AUDIO);

        int length = buffer.limit() - buffer.position();
        mTsWriter.writeAudioBuffer(mFirstPes, put(buffer, length), length, getPcr(), pts, 0, mMixed);
        mFirstPes = false;
    }

    private void writePatPmt(FrameType frameType) {
        writePatPmt(frameType, TsPacketWriter.STREAM_TYPE_VIDEO_H264);
    }
}
