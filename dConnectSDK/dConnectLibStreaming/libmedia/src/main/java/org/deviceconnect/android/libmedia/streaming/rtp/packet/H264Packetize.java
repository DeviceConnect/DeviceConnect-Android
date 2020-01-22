package org.deviceconnect.android.libmedia.streaming.rtp.packet;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacket;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;

/**
 * H264 Annex B Format を RTP のパケットに変換するクラス.
 */
public class H264Packetize extends RtpPacketize {
    /**
     * RTP と FU header を足し合わせたサイズを定義します.
     */
    private static final int HEADER_LEN = RTP_HEADER_LENGTH + 2;

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

        if (dataLength <= MAX_PACKET_SIZE - HEADER_LEN) {
            RtpPacket rtpPacket = getRtpPacket();
            byte[] dest = rtpPacket.getBuffer();

            writeRtpHeader(dest, pts);
            writeNextPacket(dest);

            System.arraycopy(data, 4, dest, RTP_HEADER_LENGTH, dataLength - 4);

            send(rtpPacket, RTP_HEADER_LENGTH + dataLength - 4, pts);
        } else {
            dataLength -= 5;

            int offset = 5;
            while (dataLength > 0) {
                RtpPacket rtpPacket = getRtpPacket();
                byte[] dest = rtpPacket.getBuffer();

                writeRtpHeader(dest, pts);

                // Set FU-A indicator
                dest[12] = (byte) (data[4] & 0xE0);
                dest[12] += 28;

                // Set FU-A header
                dest[13] = (byte) (data[4] & 0x1F);
                if (offset == 5) {
                    dest[13] += 0x80;
                }

                int length;
                if (dataLength > MAX_PACKET_SIZE - HEADER_LEN) {
                    length = MAX_PACKET_SIZE - HEADER_LEN;
                } else {
                    dest[13] += 0x40;
                    writeNextPacket(dest);
                    length = dataLength;
                }

                System.arraycopy(data, offset, dest, HEADER_LEN, length);

                send(rtpPacket, HEADER_LEN + length, pts);

                offset += length;
                dataLength -= length;
            }
        }
    }
}
