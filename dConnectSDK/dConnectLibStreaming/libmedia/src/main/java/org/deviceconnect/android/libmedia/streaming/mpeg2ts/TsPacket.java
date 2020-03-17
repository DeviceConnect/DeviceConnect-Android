package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.util.Arrays;

class TsPacket {
    /**
     * パケットデータ.
     */
    final byte[] mData = new byte[TsConstants.TS_PACKET_SIZE];

    /**
     * 書き込んでいる位置.
     */
    int mOffset = 0;

    /**
     * 指定された値を書き込みます.
     *
     * @param b 書き込む値
     */
    void add(byte b) {
        mData[mOffset++] = b;
    }

    /**
     * 指定されたデータを書き込みます.
     *
     * @param buffer バッファ
     * @param offset 書き込むバッファのオフセット
     * @param length 書き込むバッファのサイズ
     */
    void add(byte[] buffer, int offset, int length) {
        System.arraycopy(buffer, offset, mData, mOffset, length);
        mOffset += length;
    }

    /**
     * パケットのデータを指定された値で埋め尽くします.
     * @param b 埋め尽くすデータ
     */
    void reset(final byte b) {
        Arrays.fill(mData, b);
        mOffset = 0;
    }
}
