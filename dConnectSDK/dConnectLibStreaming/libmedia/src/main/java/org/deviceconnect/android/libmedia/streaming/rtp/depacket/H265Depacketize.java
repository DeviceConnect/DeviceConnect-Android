package org.deviceconnect.android.libmedia.streaming.rtp.depacket;

import java.io.ByteArrayOutputStream;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;

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

    @Override
    public void write(byte[] data, int payloadStart, int dataLength) {
        if (!checkSequenceNumber(data)) {
            mSync = false;
        }

// 0               1
// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
// |F|    Type   |  LayerId  | TID |
// +-------------+-----------------+
// Forbidden zero(F) : 1 bit
// NAL unit type(Type) : 6 bits
// NUH layer ID(LayerId) : 6 bits
// NUH temporal ID plus 1 (TID) : 3 bits

        int nal = (data[12] >> 1) & 0x3F;
        int lid = ((data[12] & 0x01) << 5) | ((data[13] >> 3) & 0x1f);
        int tid = data[13] & 0x07;

        switch (nal) {
            case 48: // aggregated packet (AP) - with two or more NAL units
                break;

            case 49: // fragmentation unit (FU)
                break;

            case 50: // TODO: 4.4.4. PACI Packets (p32)
                break;

            case 32: // video parameter set (VPS)
            case 33: // sequence parameter set (SPS)
            case 34: // picture parameter set (PPS)
            case 39: // supplemental enhancement information (SEI)
            default: // 4.4.1. Single NAL Unit Packets (p24)
                break;
        }
    }
}
