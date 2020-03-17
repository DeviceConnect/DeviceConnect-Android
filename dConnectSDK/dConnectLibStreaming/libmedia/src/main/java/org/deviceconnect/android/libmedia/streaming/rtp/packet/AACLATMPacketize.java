package org.deviceconnect.android.libmedia.streaming.rtp.packet;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacket;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;

/**
 * AAC LATM を RTP パケットに変換するためのクラス.
 */
public class AACLATMPacketize extends RtpPacketize {
    /**
     * RTP と AU header を足し合わせたサイズを定義します.
     */
    private static final int HEADER_LEN = RTP_HEADER_LENGTH + 4;

    /**
     * サンプリングレート.
     */
    private int mSamplingRate;

    public AACLATMPacketize() {
        setPayloadType(96);
        setSamplingRate(16000);
    }

    /**
     * サンプリングレートを取得します.
     *
     * @return サンプリングレート
     */
    public int getSamplingRate() {
        return mSamplingRate;
    }

    /**
     * サンプリングレートを設定します.
     *
     * @param samplingRate サンプリングレート
     */
    public void setSamplingRate(int samplingRate) {
        mSamplingRate = samplingRate;
        setClockFrequency(samplingRate);
    }

    @Override
    public void write(byte[] data, int dataLength, long pts) {
        if (data == null || data.length <= 0) {
            return;
        }

        pts = updateTimestamp(pts * 1000L);

        int count = 0;
        while (count < dataLength) {
            RtpPacket rtpPacket = getRtpPacket();
            byte[] dest = rtpPacket.getBuffer();

            writeRtpHeader(dest, pts);

            dest[RTP_HEADER_LENGTH] = 0;
            dest[RTP_HEADER_LENGTH + 1] = 0x10;

            // AU-size
            dest[RTP_HEADER_LENGTH + 2] = (byte) (dataLength >> 5);
            dest[RTP_HEADER_LENGTH + 3] = (byte) (dataLength << 3);

            // AU-Index
            dest[RTP_HEADER_LENGTH + 3] &= 0xF8;
            dest[RTP_HEADER_LENGTH + 3] |= 0x00;

            int length;
            if (dataLength - count > MAX_PACKET_SIZE - HEADER_LEN) {
                length = MAX_PACKET_SIZE - HEADER_LEN;
            } else {
                length = dataLength - count;
                writeNextPacket(dest);
            }

            System.arraycopy(data, count, dest, HEADER_LEN, length);

            send(rtpPacket, HEADER_LEN + dataLength, pts);

            count += length;
        }
    }
}
