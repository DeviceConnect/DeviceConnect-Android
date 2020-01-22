package org.deviceconnect.android.libmedia.streaming.rtp.depacket;

import android.util.Log;

import java.io.ByteArrayOutputStream;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;

public class AACLATMDepacketize extends RtpDepacketize {
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

    /**
     * サンプリングレート.
     */
    private int mSamplingRate;

    public AACLATMDepacketize() {
        setSamplingRate(44100);
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
    public synchronized void write(byte[] data, int payloadStart, int dataLength) {
        if (!checkSequenceNumber(data)) {
            mSync = false;
        }

        if (data[payloadStart] != 0x00 || data[payloadStart + 1] != 0x10) {
            Log.e("ABC", "## error");
        }

        int auSize = (data[payloadStart + 2] & 0x1F) << 5 | ((data[payloadStart + 3] >> 3) & 0x07);

        mOutputStream.write(data, payloadStart + 4, dataLength - (payloadStart + 4));

        if (isNextPacket(data)) {
            if (mSync) {
                postData(mOutputStream.toByteArray(), getTimestamp(data));
            }
            mOutputStream.reset();
            mSync = true;
        }
    }
}
