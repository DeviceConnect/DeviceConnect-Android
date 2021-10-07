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
     * FU header のサイズを定義します.
     */
    private static final int FU_HEADER_LEN = 3;

    /**
     * RTP と FU header を足し合わせたサイズを定義します.
     */
    private static final int HEADER_LEN = RTP_HEADER_LENGTH + FU_HEADER_LEN;

    @Override
    public void write(byte[] data, int dataLength, long pts) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (data[0] != 0x00 && data[1] != 0x00 && data[2] != 0x00 && data[3] != 0x01) {
            return;
        }

        pts = updateTimestamp(pts * 1000L);

        int start = 0;
        while (start < dataLength) {
            int end = searchNalUnit(data, start + 4, dataLength);
            int length = end - start;

            if (length <= MAX_PACKET_SIZE - HEADER_LEN) {
                writeSingleNALUnit(data, start, length, pts);
            } else {
                writeFragmentationUnits(data, start, length, pts);
            }

            start += length;
        }
    }

    // Single NAL Unit Packet
    // 0                   1                   2                   3
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |           PayloadHdr          |      DONL (conditional)       |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                                                               |
    // |                  NAL unit payload data                        |
    // |                                                               |
    // |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                               :...OPTIONAL RTP padding        |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    private void writeSingleNALUnit(byte[] data, int offset, int dataLength, long pts) {
        RtpPacket rtpPacket = getRtpPacket();
        byte[] dest = rtpPacket.getBuffer();

        writeRtpHeader(dest, pts);
        writeNextPacket(dest);

        System.arraycopy(data, offset + 4, dest, RTP_HEADER_LENGTH, dataLength - 4);

        send(rtpPacket, RTP_HEADER_LENGTH + dataLength - 4, pts);
    }

    // HEVC NAL Unit Header
    // +---------------+---------------+
    // |0|1|2|3|4|5|6|7|0|1|2|3|4|5|6|7|
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |F|   Type    |  LayerId  | TID |
    // +-------------+-----------------+

    // Fragmentation Units (FUs)
    // 0                   1                   2                   3
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |    PayloadHdr (Type=49)       |   FU header   | DONL (cond)   |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
    // | DONL (cond)   |                                               |
    // |-+-+-+-+-+-+-+-+                                               |
    // |                         FU payload                            |
    // |                                                               |
    // |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                               :...OPTIONAL RTP padding        |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    // FU header
    // +---------------+
    // |0|1|2|3|4|5|6|7|
    // +-+-+-+-+-+-+-+-+
    // |S|E|  FuType   |
    // +---------------+

    private void writeFragmentationUnits(byte[] data, int srcOffset, int dataLength, long pts) {
        byte naluType = (byte) ((data[srcOffset + 4] >> 1) & 0x3F);

        dataLength -= 6;

        int offset = srcOffset + 6;
        while (dataLength > 0) {
            RtpPacket rtpPacket = getRtpPacket();
            byte[] dest = rtpPacket.getBuffer();

            writeRtpHeader(dest, pts);

            dest[RTP_HEADER_LENGTH] = 49 << 1;
            dest[RTP_HEADER_LENGTH + 1] = 1;
            dest[RTP_HEADER_LENGTH + 2] = naluType;
            if (offset == srcOffset + 6) {
                // set start flag
                dest[RTP_HEADER_LENGTH + 2] += 0x80;
            }

            int length;
            if (dataLength > MAX_PACKET_SIZE - HEADER_LEN) {
                length = MAX_PACKET_SIZE - HEADER_LEN;
            } else {
                // set end flag
                dest[RTP_HEADER_LENGTH + 2] += 0x40;
                writeNextPacket(dest);
                length = dataLength;
            }

            System.arraycopy(data, offset, dest, HEADER_LEN, length);

            send(rtpPacket, HEADER_LEN + length, pts);

            offset += length;
            dataLength -= length;
        }
    }

    private int searchNalUnit(byte[] data, int offset, int dataLength) {
        // NAL_UNIT_VPS (32) よりも小さい nalu type の場合は、後ろに nalu unit が
        // 存在する端末がなかったので、ここでは最後までのデータサイズを返却するようにする。
        int naluType = (data[offset] >> 1) & 0x3F;
        if (naluType < 32) {
            return dataLength;
        }

        for (int i = offset; i < dataLength - 4; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                return i;
            }
        }
        return dataLength;
    }
}
