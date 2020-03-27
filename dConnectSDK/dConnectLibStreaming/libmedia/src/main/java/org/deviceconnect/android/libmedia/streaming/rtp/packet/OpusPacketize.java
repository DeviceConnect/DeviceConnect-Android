package org.deviceconnect.android.libmedia.streaming.rtp.packet;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacket;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;

public class OpusPacketize extends RtpPacketize {

    public OpusPacketize() {
        this(111);
    }

    public OpusPacketize(int pt) {
        setPayloadType(pt);
        setClockFrequency(48000);
    }

    @Override
    public void write(byte[] data, int dataLength, long pts) {
        pts = updateTimestamp(pts * 1000L);

        RtpPacket rtpPacket = getRtpPacket();
        byte[] dest = rtpPacket.getBuffer();

        writeRtpHeader(dest, pts);
        writeNextPacket(dest);

        System.arraycopy(data, 0, dest, RTP_HEADER_LENGTH, dataLength);

        send(rtpPacket, RTP_HEADER_LENGTH + dataLength, pts);
    }
}
