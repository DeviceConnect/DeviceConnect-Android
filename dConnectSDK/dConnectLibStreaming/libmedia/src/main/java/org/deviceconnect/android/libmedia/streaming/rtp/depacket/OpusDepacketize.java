package org.deviceconnect.android.libmedia.streaming.rtp.depacket;

import java.io.ByteArrayOutputStream;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;

public class OpusDepacketize extends RtpDepacketize {
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

    public OpusDepacketize() {
        setClockFrequency(48000);
    }

    @Override
    public void write(byte[] data, int payloadStart, int dataLength) {
        if (!checkSequenceNumber(data)) {
            mSync = false;
        }

        mOutputStream.write(data, payloadStart, dataLength - payloadStart);

//        if (isNextPacket(data)) {
            if (mSync) {
                postData(mOutputStream.toByteArray(), getTimestamp(data));
            }
            mOutputStream.reset();
            mSync = true;
//        }
    }
}
