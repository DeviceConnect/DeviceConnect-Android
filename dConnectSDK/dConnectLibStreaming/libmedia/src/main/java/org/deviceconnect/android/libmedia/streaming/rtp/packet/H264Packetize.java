package org.deviceconnect.android.libmedia.streaming.rtp.packet;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacket;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;

/**
 * H264 Annex B Format を RTP のパケットに変換するクラス.
 *
 * 参考: https://tools.ietf.org/html/rfc3984
 */
public class H264Packetize extends RtpPacketize {
    /**
     * FU header のサイズを定義します.
     */
    private static final int FU_HEADER_LEN = 2;

    /**
     * RTP と FU header を足し合わせたサイズを定義します.
     */
    private static final int HEADER_LEN = RTP_HEADER_LENGTH + FU_HEADER_LEN;

    /**
     * コンストラクタ.
     * <p>
     * デフォルトでは、以下の値を設定します。
     * <ul>
     * <li>PayloadType: 96</li>
     * <li>sClockFrequency: 90000</li>
     * </ul>
     * </p>
     */
    public H264Packetize() {
        setPayloadType(96);
        setClockFrequency(90000);
    }

    /**
     * コンストラクタ.
     *
     * @param pt ペイロードタイプ
     */
    public H264Packetize(int pt) {
        setPayloadType(pt);
        setClockFrequency(90000);
    }

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
    // |F|NRI|  type   |                                               |
    // +-+-+-+-+-+-+-+-+                                               |
    // |                                                               |
    // |               Bytes 2..n of a Single NAL unit                 |
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

    // NAL Unit Header
    // +---------------+
    // |0|1|2|3|4|5|6|7|
    // +-+-+-+-+-+-+-+-+
    // |F|NRI|  Type   |
    // +---------------+

    // Fragmentation Units (FUs)
    // 0                   1                   2                   3
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // | FU indicator  |   FU header   |               DON             |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
    // |                                                               |
    // |                         FU payload                            |
    // |                                                               |
    // |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                               :...OPTIONAL RTP padding        |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    // FU indicator
    // +---------------+
    // |0|1|2|3|4|5|6|7|
    // +-+-+-+-+-+-+-+-+
    // |F|NRI|  Type   |
    // +---------------+

    // FU Header
    //+---------------+
    //|0|1|2|3|4|5|6|7|
    //+-+-+-+-+-+-+-+-+
    //|S|E|R|  Type   |
    //+---------------+

    private void writeFragmentationUnits(byte[] data, int srcOffset, int dataLength, long pts) {
        dataLength -= 5;

        int offset = srcOffset + 5;
        while (dataLength > 0) {
            RtpPacket rtpPacket = getRtpPacket();
            byte[] dest = rtpPacket.getBuffer();

            writeRtpHeader(dest, pts);

            // Set FU-A indicator
            dest[RTP_HEADER_LENGTH] = (byte) (data[srcOffset + 4] & 0xE0);
            dest[RTP_HEADER_LENGTH] += 28; // (FU-A)

            // Set FU-A header
            dest[RTP_HEADER_LENGTH + 1] = (byte) (data[srcOffset + 4] & 0x1F);
            if (offset == srcOffset + 5) {
                // set start flag
                dest[RTP_HEADER_LENGTH + 1] += 0x80;
            }

            int length;
            if (dataLength > MAX_PACKET_SIZE - HEADER_LEN) {
                length = MAX_PACKET_SIZE - HEADER_LEN;
            } else {
                // set end flag
                dest[RTP_HEADER_LENGTH + 1] += 0x40;
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
        // NALU_TYPE_SEI (6) よりも小さい nalu type の場合は、後ろに nalu unit が
        // 存在する端末がなかったので、ここでは最後までのデータサイズを返却するようにする。
        int naluType = data[offset] & 0x1F;
        if (naluType < 6) {
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
