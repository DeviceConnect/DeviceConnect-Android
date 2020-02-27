package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;

public class AacH265TsPacketWriter extends AacH26xTsPacketWriter {
    @Override
    public void writeNALU(ByteBuffer buffer, long pts) {
        writePatPmt(FrameType.VIDEO);

        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = ((buffer.get(4) >> 1) & 0x3F);
        boolean isFrame = type <= 20;
        buffer.position(offset);
        mTsWriter.writeVideoBuffer(mFirstPes, put(buffer, length), length, getPcr(), pts, 0, isFrame, mMixed);
        mFirstPes = false;
    }

    @Override
    public void writeADTS(ByteBuffer buffer, long pts) {
        writePatPmt(FrameType.AUDIO);

        int length = buffer.limit() - buffer.position();
        mTsWriter.writeAudioBuffer(mFirstPes, put(buffer, length), length, getPcr(), pts, 0, mMixed);
        mFirstPes = false;
    }

    private void writePatPmt(FrameType frameType) {
        writePatPmt(frameType, TsPacketWriter.STREAM_TYPE_VIDEO_H265);
    }
}
