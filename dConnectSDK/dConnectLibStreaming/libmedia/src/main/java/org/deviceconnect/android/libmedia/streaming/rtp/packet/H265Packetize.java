package org.deviceconnect.android.libmedia.streaming.rtp.packet;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacket;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;

/**
 * H265 を RTP のパケットに変換するクラス.
 *
 * 参考: https://tools.ietf.org/html/rfc7798
 */
public class H265Packetize extends RtpPacketize {
    /**
     * RTP と FU header を足し合わせたサイズを定義します.
     */
    private static final int HEADER_LEN = RTP_HEADER_LENGTH + 2;

    @Override
    public void write(byte[] data, int dataLength, long pts) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (data[0] != 0x00 && data[1] != 0x00 && data[2] != 0x00 && data[3] != 0x01) {
            return;
        }

        if (dataLength <= MAX_PACKET_SIZE - HEADER_LEN) {
            RtpPacket rtpPacket = getRtpPacket();
            byte[] dest = rtpPacket.getBuffer();

            writeRtpHeader(dest, pts);
            writeNextPacket(dest);

            System.arraycopy(data, 4, dest, RTP_HEADER_LENGTH, dataLength - 4);

            send(rtpPacket, RTP_HEADER_LENGTH + dataLength - 4, pts);
        } else {
        }
    }
}
