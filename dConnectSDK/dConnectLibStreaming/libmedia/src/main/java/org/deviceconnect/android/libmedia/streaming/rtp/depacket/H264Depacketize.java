package org.deviceconnect.android.libmedia.streaming.rtp.depacket;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;

public class H264Depacketize extends RtpDepacketize {
    /**
     * データを格納するバッファ.
     */
    private final Buffer mOutputStream = new Buffer();

    /**
     * RTP ヘッダーのシーケンス番号の同期フラグ.
     * <p>
     * シーケンス番号の同期が取れている場合は true、それ以外はfalse。
     * </p>
     */
    private boolean mSync = true;

    public H264Depacketize() {
        setClockFrequency(90000);
    }

    @Override
    public synchronized void write(byte[] data, int payloadStart, int dataLength) {
        if (!checkSequenceNumber(data)) {
            mSync = false;
        }

        int type = data[payloadStart] & 0x1F;
        switch (type) {
            case 0:  // reserved
            case 31: // reserved
                break;
            case 24: // STAP-A
                decodeSTAP(data, payloadStart, dataLength, false);
                break;
            case 25: // STAP-B
                decodeSTAP(data, payloadStart, dataLength, true);
                break;
            case 26: // MTAP16
                decodeMTAP(data, payloadStart, dataLength, false);
                break;
            case 27: // MTAP24
                decodeMTAP(data, payloadStart, dataLength, true);
                break;
            case 28: // FU-A
                decodeFu(data, payloadStart, dataLength, false);
                break;
            case 29: // FU-B
                decodeFu(data, payloadStart, dataLength, true);
                break;
            default: // 1-23 NAL unit
                decodeSingleNalu(data, payloadStart, dataLength);
                break;
        }
    }

    /**
     * Single NALU をデコードします.
     *
     * @param data RTP パケットデータ
     * @param dataLength RTP パケットデータサイズ
     */
    private void decodeSingleNalu(byte[] data, int payloadStart, int dataLength) {
        mOutputStream.reset();
        mOutputStream.write(0x00);
        mOutputStream.write(0x00);
        mOutputStream.write(0x00);
        mOutputStream.write(0x01);
        mOutputStream.write(data, payloadStart, dataLength - payloadStart);
        postData(mOutputStream.getData(), mOutputStream.getLength(), getTimestamp(data));
    }

    /**
     * STAP-A、STAP-B をデコードします.
     *
     * @param data RTP パケットデータ
     * @param dataLength RTP パケットデータサイズ
     * @param isStapB STAP-B の場合はtrue、それ以外はfalse
     */
    private void decodeSTAP(byte[] data, int payloadStart, int dataLength, boolean isStapB) {
        int stapHeader = data[payloadStart];
        int don = isStapB ? ((data[payloadStart + 1] << 8) | data[payloadStart + 2]) : 0;

        payloadStart += (isStapB ? 3 : 1);

        while (payloadStart < dataLength) {
            int naluSize = ((data[payloadStart] & 0xFF) << 8) | (data[payloadStart + 1] & 0xFF);
            if (payloadStart + naluSize + 2 > dataLength) {
                mSync = false;
                return;
            }

            payloadStart += 2;

            mOutputStream.reset();
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x01);
            mOutputStream.write(data, payloadStart, naluSize);

            postData(mOutputStream.getData(), mOutputStream.getLength(), getTimestamp(data));

            payloadStart += naluSize;
        }
    }

    /**
     * MTAP16、MTAP24 をデコードします.
     *
     * @param data RTP パケットデータ
     * @param dataLength RTP パケットデータサイズ
     * @param isMTAP24 MTAP24 の場合はtrue、それ以外はfalse
     */
    private void decodeMTAP(byte[] data, int payloadStart, int dataLength, boolean isMTAP24) {
        int mtap16Header = data[payloadStart];
        int donb = (data[payloadStart + 1] << 8) | data[payloadStart + 2];

        payloadStart += 3;

        while (payloadStart < dataLength) {
            int naluSize = ((data[payloadStart] & 0xFF) << 8) | (data[payloadStart + 1] & 0xFF);
            if (payloadStart + naluSize + 2 > dataLength) {
                mSync = false;
                return;
            }

            int naluDOND = data[payloadStart + 2];
            int ts = ((data[payloadStart + 3] & 0xFF) << 8) | (data[payloadStart + 4] & 0xFF);

            if (isMTAP24) {
                ts = (ts << 8) | data[payloadStart + 5];
            }

            payloadStart += isMTAP24 ? 5 : 4;

            mOutputStream.reset();
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x01);
            mOutputStream.write(data, payloadStart, naluSize);

            postData(mOutputStream.getData(), mOutputStream.getLength(), ts + getTimestamp(data));

            payloadStart += naluSize;
        }
    }

    /**
     * FU-A、FU-B をデコードします.
     *
     * @param data RTP パケットデータ
     * @param dataLength RTP パケットデータサイズ
     * @param isFuB FU-B の場合はtrue、それ以外はfalse
     */
    private void decodeFu(byte[] data, int payloadStart, int dataLength, boolean isFuB) {
        int naluHeader = data[payloadStart] & 0xFF;
        int fuHeader = data[payloadStart + 1] & 0xFF;

        boolean startBit = (fuHeader & 0x80) != 0;
        boolean endBit = (fuHeader & 0x40) != 0;

        if (startBit) {
            mOutputStream.reset();
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x01);
            mOutputStream.write((naluHeader & 0xE0) | (fuHeader & 0x1F));
            mSync = true;
        }

        int fuHeaderSize = isFuB ? (payloadStart + 4) : (payloadStart + 2);

        mOutputStream.write(data, fuHeaderSize, dataLength - fuHeaderSize);

        if (endBit && mSync) {
            postData(mOutputStream.getData(), mOutputStream.getLength(), getTimestamp(data));
        }
    }
}
