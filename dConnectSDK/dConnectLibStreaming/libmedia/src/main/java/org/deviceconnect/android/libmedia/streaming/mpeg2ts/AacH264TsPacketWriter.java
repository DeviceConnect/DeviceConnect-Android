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

    private final TsPacketWriter.PES mPES = new TsPacketWriter.PES();

    @Override
    public synchronized void writeNALU(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.VIDEO);

        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = buffer.get(4) & 0x1F;
        boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
        buffer.position(offset);

        mPES.setStreamId(TsPacketWriter.STREAM_ID_VIDEO);
        mPES.setStreamType(TsPacketWriter.STREAM_TYPE_VIDEO_H264);
        mPES.setPCR(getPcr());
        mPES.setPTS(pts);
        mPES.setFrame(isFrame);
        mPES.setData(buffer, length);

        mTsWriter.writePES(mPES);
    }

    @Override
    public synchronized void writeADTS(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.AUDIO);

        int length = buffer.limit() - buffer.position();
        mPES.setStreamId(TsPacketWriter.STREAM_ID_AUDIO);
        mPES.setStreamType(TsPacketWriter.STREAM_TYPE_AUDIO_AAC);
        mPES.setPCR(getPcr());
        mPES.setPTS(pts);
        mPES.setFrame(true);
        mPES.setData(buffer, length);

        mTsWriter.writePES(mPES);
    }

    private void writePatPmt(FrameType frameType) {
        writePatPmt(frameType, TsPacketWriter.STREAM_TYPE_VIDEO_H264);
    }
}
