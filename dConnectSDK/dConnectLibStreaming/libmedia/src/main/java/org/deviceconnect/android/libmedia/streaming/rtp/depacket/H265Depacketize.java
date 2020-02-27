package org.deviceconnect.android.libmedia.streaming.rtp.depacket;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;

import java.io.ByteArrayOutputStream;

public class H265Depacketize extends RtpDepacketize {
    /**
     * データを格納するバッファ.
     */
    private final ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();

    /**
     * RTP ヘッダーのシーケンス番号の同期フラグ.
     * <p>
     * シーケンス番号の同期が取れている場合は true、それ以外はfalse。
     * </p>
     */
    private boolean mSync = true;

    // HEVC NAL Unit Header
    // 0               1
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |F|    Type   |  LayerId  | TID |
    // +-------------+-----------------+
    // Forbidden zero(F) : 1 bit
    // NAL unit type(Type) : 6 bits
    // NUH layer ID(LayerId) : 6 bits
    // NUH temporal ID plus 1 (TID) : 3 bits

    @Override
    public void write(byte[] data, int payloadStart, int dataLength) {
        if (!checkSequenceNumber(data)) {
            mSync = false;
        }

        int nal = (data[12] >> 1) & 0x3F;
        int lid = ((data[12] & 0x01) << 5) | ((data[13] >> 3) & 0x1F);
        int tid = data[13] & 0x07;

        switch (nal) {
            case 48: // aggregated packet (AP) - with two or more NAL units
                break;

            case 49: // fragmentation unit (FU)
                decodeFragmentationUnits(data, payloadStart, dataLength);
                break;

            case 50: // TODO: 4.4.4. PACI Packets (p32)
                break;

            case 32: // video parameter set (VPS)
            case 33: // sequence parameter set (SPS)
            case 34: // picture parameter set (PPS)
            case 39: // supplemental enhancement information (SEI)
            default: // 4.4.1. Single NAL Unit Packets (p24)
                decodeSingleNalu(data, payloadStart, dataLength);
                break;
        }
    }

    /**
     * Single NALU をデコードします.
     *
     * @param data RTP パケットデータ
     * @param payloadStart パケット開始位置
     * @param dataLength RTP パケットデータサイズ
     */
    private void decodeSingleNalu(byte[] data, int payloadStart, int dataLength) {
        mOutputStream.reset();
        mOutputStream.write(0x00);
        mOutputStream.write(0x00);
        mOutputStream.write(0x00);
        mOutputStream.write(0x01);
        mOutputStream.write(data, payloadStart, dataLength - payloadStart);
        postData(mOutputStream.toByteArray(), getTimestamp(data));
    }

    private void decodeFragmentationUnits(byte[] data, int payloadStart, int dataLength) {
        int fuHeader = data[payloadStart + 2] & 0xFF;

        boolean startBit = (fuHeader & 0x80) != 0;
        boolean endBit = (fuHeader & 0x40) != 0;

        if (startBit) {
            mOutputStream.reset();
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x00);
            mOutputStream.write(0x01);
            mOutputStream.write(((fuHeader & 0x3F) << 1));
            mSync = true;
        }

        int fuHeaderSize = 3;

        mOutputStream.write(data, fuHeaderSize, dataLength - fuHeaderSize);

        if (endBit && mSync) {
            postData(mOutputStream.toByteArray(), getTimestamp(data));
        }
    }
}
