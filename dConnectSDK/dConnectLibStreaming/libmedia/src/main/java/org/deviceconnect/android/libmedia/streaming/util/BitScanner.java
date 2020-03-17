package org.deviceconnect.android.libmedia.streaming.util;

/**
 * Bitストリームを読み込むためのスキャナ.
 */
class BitScanner {
    /**
     * 現在のビット位置.
     */
    private int mCurrentBit;

    /**
     * 読み込むデータ.
     */
    private byte[] mData;

    /**
     * コンストラクタ.
     * @param data ビットで読み込むデータ
     */
    BitScanner(byte[] data) {
        mData = data;
    }

    /**
     * オフセットを設定します.
     *
     * @param offset オフセット(byte)
     */
    void setOffset(int offset) {
        mCurrentBit += offset * 8;
    }

    /**
     * 1 bit 読み込みます.
     *
     * @return 0 or 1
     */
    int readBit() {
        int index = mCurrentBit / 8;
        int offset = mCurrentBit % 8 + 1;
        mCurrentBit++;
        return (mData[index] >> (8 - offset)) & 0x01;
    }

    /**
     * 指定された n bit 分だけデータを読み込みます.
     *
     * @param n 読み込むビット数
     * @return 値
     */
    int readBits(int n) {
        int r = 0;
        for (int i = 0; i < n; i++) {
            r |= (readBit() << (n - i - 1));
        }
        return r;
    }

    /**
     * Exp-Golomb codes の値を読み込みます.
     *
     * 9.2 Parsing process for 0-th order Exp-Golomb codes.
     *
     * @return 値
     */
    int readExponentialGolombCode() {
        int r = 0;
        int i = 0;

        while ((readBit() == 0) && i < 32) {
            i++;
        }

        if (i > 0) {
            r = readBits(i);
            r += ((1 << i) - 1);
        }
        return r;
    }

    /**
     * ue(v) の値を読み込みます.
     *
     * @return 値
     */
    int readUE() {
        return readExponentialGolombCode();
    }

    /**
     * se(v) の値を読み込みます.
     *
     * @return 値
     */
    int readSE() {
        int r = readExponentialGolombCode();
        if ((r & 0x01) != 0) {
            r = (r + 1) / 2;
        } else {
            r = -(r / 2);
        }
        return r;
    }
}
