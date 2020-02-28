package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;

public class AacH265TsPacketWriter extends AacH26xTsPacketWriter {

    private final TsPacketWriter.PES mPES = new TsPacketWriter.PES();

    @Override
    public synchronized void writeNALU(ByteBuffer buffer, long pts) {
        writePatPmt(FrameType.VIDEO);

        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = ((buffer.get(4) >> 1) & 0x3F);
        boolean isFrame = type < 32;
        buffer.position(offset);

        mPES.setStreamId(TsPacketWriter.STREAM_ID_VIDEO);
        mPES.setStreamType(TsPacketWriter.STREAM_TYPE_VIDEO_H265);
        mPES.setPCR(getPcr());
        mPES.setPTS(pts);
        mPES.setFrame(isFrame);
        mPES.setData(buffer, length);

        mTsWriter.writePES(mPES);
    }

    @Override
    public synchronized void writeADTS(ByteBuffer buffer, long pts) {
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
        writePatPmt(frameType, TsPacketWriter.STREAM_TYPE_VIDEO_H265);
    }
}
