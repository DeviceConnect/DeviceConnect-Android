package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.util.Arrays;

import androidx.annotation.NonNull;

class Buffer {
    /**
     * データを格納するバッファ.
     */
    byte[] mData;

    /**
     * データを読み込んでいる位置.
     */
    int mPosition;

    /**
     * データサイズ.
     */
    int mLength;

    /**
     * 使用フラグ.
     */
    boolean mConsumable;

    /**
     * コンストラクタ.
     *
     * @param dataLength データサイズ
     */
    Buffer(int dataLength) {
        mData = new byte[dataLength];
        mLength = dataLength;
        mPosition = 0;
        mConsumable = false;
    }

    /**
     * コンストラクタ.
     *
     * <p>
     * 引数に渡されたデータを内部のバッファにデータサイズ分だけコピーします。
     * </p>
     *
     * @param data       データ
     * @param dataLength データサイズ
     */
    Buffer(byte[] data, int dataLength) {
        this(dataLength);
        System.arraycopy(data, 0, mData, 0, dataLength);
    }

    /**
     * データをコピーします.
     *
     * @param data       コピーするデータ
     * @param dataLength コピーするデータのサイズ
     */
    void setData(byte[] data, int dataLength) {
        mPosition = 0;
        mLength = dataLength;
        System.arraycopy(data, 0, mData, 0, dataLength);
    }

    /**
     * 残りバイト数を取得します.
     *
     * @return 残りバイト数
     */
    int remaining() {
        return mLength - mPosition;
    }

    /**
     * 読み込み位置がデータの最後まで来ているか確認します.
     *
     * @return 最後まで読み込まれていた場合はtrue、それ以外はfalse
     */
    boolean isEOF() {
        return mPosition >= mLength;
    }

    /**
     * 1byte読み込み、読み込み位置を1ずらします.
     *
     * @return バイトデータ
     */
    int read() {
        return mData[mPosition++] & 0xFF;
    }

    /**
     * 指定されたサイズ分のデータを読み込みます.
     *
     * @param target 読み込んだデータを格納するバッファ
     * @param offset データを格納するバッファのオフセット
     * @param length 読み込むデータサイズ
     * @return 読み込んだデータサイズ
     */
    int read(byte[] target, int offset, int length) {
        int len = length;
        int remaining = remaining();
        if (remaining < length) {
            len = remaining;
        }
        System.arraycopy(mData, mPosition, target, offset, len);
        mPosition += len;
        return len;
    }

    /**
     * 1byte 取得します.
     *
     * <p>
     * {@link #read()} とは違い、読み込み位置は移動しません。
     * </p>
     *
     * @param offset オフセット
     * @return 指定されたバッファの値
     */
    int peek(int offset) {
        return mData[mPosition + offset] & 0xFF;
    }

    /**
     * 指定されたバイト数だけスキップします.
     *
     * @param length スキップするサイズ
     */
    int skip(int length) {
        int len = length;
        int remaining = remaining();
        if (remaining < length) {
            len = remaining;
        }
        mPosition += len;
        return len;
    }

    /**
     * フレームバッファの使用中フラグを取得します.
     *
     * @return フレームバッファの使用中フラグ
     */
    boolean isConsumable() {
        return mConsumable;
    }

    /**
     * フレームバッファを使用します.
     */
    void consume() {
        mConsumable = true;
    }

    /**
     * フレームバッファを解放します.
     */
    void release() {
        mConsumable = false;
    }


    @NonNull
    @Override
    public String toString() {
        return "Buffer{" +
                "mData=" + Arrays.toString(mData) +
                ", mPosition=" + mPosition +
                ", mLength=" + mLength +
                '}';
    }
}
